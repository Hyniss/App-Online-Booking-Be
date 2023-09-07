package com.fpt.h2s.repositories;

import ananta.utility.StringEx;
import com.fpt.h2s.models.entities.Room;
import com.fpt.h2s.models.entities.RoomProperty;
import com.fpt.h2s.repositories.projections.AvailableRoomRecord;
import com.fpt.h2s.utilities.MoreJPA;
import com.fpt.h2s.utilities.SpringBeans;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.*;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    List<Room> findWithLockingByIdIn(Collection<Integer> ids);

    List<Room> findAllByAccommodationId(Integer accommodationId);

    @Query(nativeQuery = true, value =
        """
            SELECT sum(booking_requests.total_rooms) as rooms\s
            FROM rooms
            inner join booking_requests on booking_requests.room_id = rooms.id
            where\s
            	rooms.id = :roomId and
                booking_requests.status not in ('CANCELED', 'REJECTED') and
                checkin_at >= :time
            """)
    Integer findAvailableRoomsOfAccommodationBetween(Timestamp time, Integer roomId);

    @Query(nativeQuery = true, value =
        """
            SELECT
            	rooms.id,
                rooms.name,
                (rooms.count - IFNULL(booked_rooms.rooms_left, 0)) as availableRooms
            FROM (
            	SELECT rooms.id, sum(details.total_rooms) as rooms_left
            	FROM rooms
            	inner join booking_requests_details details on details.room_id = rooms.id
            	inner join booking_requests on details.booking_request_id = booking_requests.id
            	where
                    rooms.accommodation_id = :accommodationId AND
                    booking_requests.status NOT IN ('CANCELED', 'UN_PURCHASED') AND
                    (
                        (booking_requests.checkin_at BETWEEN :start AND :end) OR
                        (booking_requests.checkout_at BETWEEN :start AND :end) OR
                        (:start BETWEEN booking_requests.checkin_at AND booking_requests.checkout_at) OR
                        (:end BETWEEN booking_requests.checkin_at AND booking_requests.checkout_at)
                    )
            	group by rooms.id
            ) as booked_rooms
            RIGHT JOIN rooms on booked_rooms.id = rooms.id
            WHERE
            	rooms.accommodation_id = :accommodationId and
            	rooms.status = 'OPENING' and
                (-1 in :roomIds or rooms.id in :roomIds)
            """)
    List<AvailableRoomRecord> findAvailableRoomsOfAccommodationBetween(Timestamp start, Timestamp end, Integer accommodationId, Collection<Integer> roomIds);

    Optional<Room> findByIdAndAccommodationId(Integer id, Integer accommodationId);

    @Query(nativeQuery = true, value = """
        select r.* from rooms r\s
        inner join rooms_properties beds_property on r.id  = beds_property.room_id\s
        and beds_property.key_id = 1 and beds_property.value >= :bedsValue\s
        inner join rooms_properties adults_property on r.id  = adults_property.room_id\s
        and adults_property.key_id = 4 and adults_property.value >= :adultsValue\s
        inner join rooms_properties children_property on r.id  = children_property.room_id\s
        and children_property.key_id = 5 and children_property.value >= :childrenValue\s
        inner join rooms_properties pets_property on r.id  = pets_property.room_id\s
        and pets_property.key_id = 6 and pets_property.value >= :petsValue\s
        where r.accommodation_id = :accommodationId\s
        """)
    List<Room> findRoomByProperty(@Param("bedsValue") Integer numOfBeds,
                                  @Param("adultsValue") Integer numOfAdults,
                                  @Param("childrenValue") Integer numOfChildren,
                                  @Param("petsValue") Integer numOfPets,
                                  @Param("accommodationId") Integer accommodationId);

    List<Room> findByAccommodationId(Integer accommodationId);

    @NotNull
    default Set<Integer> getRoomsThatMeet(Specification<RoomProperty> criteria, int criteriaSize) {
        String whereClause = StringEx.afterOf("where", MoreJPA.toSqlString(criteria, RoomProperty.class).toLowerCase());
        String query = """
                SELECT room_id as id
                FROM (
                    SELECT room_id, COUNT(*) AS count
                    FROM rooms_properties r1_0
                    WHERE %s
                    GROUP BY room_id
                    HAVING count = %s
                ) as result
            """.formatted(whereClause, criteriaSize);

        EntityManager entityManager = SpringBeans.getBean(EntityManager.class);
        List<Integer> resultList = entityManager.createNativeQuery(query).getResultList();
        return new HashSet<>(resultList);
    }


    @Query(nativeQuery = true, value = """
        WITH room_prices AS (
            (
                WITH custom_dates AS (
                    SELECT dates.date
                    FROM
                        price_histories price
                        JOIN dates ON date BETWEEN price.from_date AND price.to_date
                    WHERE
                        day_type = 'CUSTOM' AND (-1 in :roomIds or room_id in :roomIds)
                )
                SELECT room_id, amount, price.type, price.day_type AS date_type, dates.date AS value
                FROM
                    price_histories price
                    INNER JOIN dates ON price.day_type = dates.type
                    LEFT JOIN custom_dates ON dates.date = custom_dates.date
                WHERE
                    (-1 in :roomIds or room_id in :roomIds) AND custom_dates.date IS NULL AND dates.date BETWEEN :fromDate AND :toDate
            )
            UNION
            (
                SELECT room_id, amount, price.type, price.day_type AS date_type, dates.date AS value
                FROM
                    price_histories price
                    JOIN dates ON date BETWEEN price.from_date
                    AND price.to_date
                WHERE
                    day_type = 'CUSTOM'
                    AND (-1 in :roomIds or room_id in :roomIds)
                    AND dates.date BETWEEN :fromDate AND :toDate
            )
        )
        SELECT room_id, sum(price) as price, sum(price * (100 - IFNULL(discount, 0)) / 100) AS display_price
        FROM (
        	SELECT
        		prices.room_id, price, discount, prices.value as date
        	FROM
        		(
                    SELECT room_id, (rooms.price * amount / 100) AS price, value
                    FROM room_prices
                    INNER JOIN rooms on room_prices.room_id = rooms.id
                    WHERE type = 'PRICE'
        		) AS prices
        		LEFT JOIN (
        			SELECT room_id, amount as discount, value
        			FROM room_prices
        			WHERE type = 'DISCOUNT'
        		) AS discounts ON prices.room_id = discounts.room_id and prices.value = discounts.value
        ) AS rooms_details GROUP BY room_id
        HAVING display_price BETWEEN :fromPrice AND :toPrice
        """)
    Set<Integer> findAllRoomsHavingTotalPriceBetween(Long fromPrice, Long toPrice, Timestamp fromDate, Timestamp toDate, Collection<Integer> roomIds);

    @Query(nativeQuery = true, value = """
        WITH room_prices AS (
            (
                WITH custom_dates AS (
                    SELECT dates.date
                    FROM
                        price_histories price
                        JOIN dates ON date BETWEEN price.from_date AND price.to_date
                    WHERE
                        day_type = 'CUSTOM' AND (-1 in :roomIds or room_id in :roomIds)
                )
                SELECT room_id, amount, price.type, price.day_type AS date_type, dates.date AS value
                FROM
                    price_histories price
                    INNER JOIN dates ON price.day_type = dates.type
                    LEFT JOIN custom_dates ON dates.date = custom_dates.date
                WHERE
                    (-1 in :roomIds or room_id in :roomIds) AND custom_dates.date IS NULL AND dates.date BETWEEN :fromDate AND :toDate
            )
            UNION
            (
                SELECT room_id, amount, price.type, price.day_type AS date_type, dates.date AS value
                FROM
                    price_histories price
                    JOIN dates ON date BETWEEN price.from_date
                    AND price.to_date
                WHERE
                    day_type = 'CUSTOM'
                    AND (-1 in :roomIds or room_id in :roomIds)
                    AND dates.date BETWEEN :fromDate AND :toDate
            )
        )
        SELECT prices.room_id, price, discount, prices.value AS date, price * (100 - IFNULL(discount, 0)) / 100 AS display_price
        FROM (
            SELECT room_id, (rooms.price * amount / 100) AS price, value
            FROM room_prices
            INNER JOIN rooms on room_prices.room_id = rooms.id
            WHERE type = 'PRICE'
        ) AS prices
        LEFT JOIN (
            SELECT room_id, amount AS discount, value
            FROM room_prices
            WHERE type = 'DISCOUNT'
        ) AS discounts ON prices.room_id = discounts.room_id AND prices.value = discounts.value
        HAVING display_price BETWEEN :fromPrice AND :toPrice
        """)
    Set<Integer> findAllRoomsHavingEveryDayPriceBetween(Long fromPrice, Long toPrice, Timestamp fromDate, Timestamp toDate, Collection<Integer> roomIds);
}
