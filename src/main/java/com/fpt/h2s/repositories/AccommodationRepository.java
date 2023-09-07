package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.repositories.projections.IdHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface AccommodationRepository extends BaseRepository<Accommodation, Integer> {

    Page<Accommodation> findAll(Specification<Accommodation> specification, Pageable pageable);

    Page<Accommodation> findAllByOwnerIdAndStatus(Integer userId, Accommodation.Status status, Pageable pageable);

    @Query(value = """
        SELECT accommodation_id as id
        FROM (
        	SELECT accommodation_id, count(*) as total
        	FROM
        		room_amenities
        	INNER JOIN categories ON room_amenities.category_id = categories.id
            INNER JOIN rooms on rooms.id  = room_amenities.room_id
        	WHERE
        		type = 'AMENITY' and
        		category_id IN :categories
        		AND (
        			-1 IN :accommodationIds
        			OR rooms.accommodation_id IN :accommodationIds
        		)
        	GROUP BY accommodation_id
        	HAVING total = :categoriesSize
        ) as result
        """, nativeQuery = true)
    List<IdHolder> findAllByRoomHavingAmenities(Collection<Integer> categories, Integer categoriesSize, Collection<Integer> accommodationIds);

    @Query(value = """
        SELECT accommodation_id as id
        FROM (
            SELECT accommodation_id, COUNT(*) AS count
            FROM accommodations_categories
            INNER JOIN categories ON accommodations_categories.category_id = categories.id
            WHERE
                type = 'LOCATION' and
                category_id IN :viewIds
            GROUP BY accommodation_id
            HAVING count = :totalCategories
        ) as result
            """, nativeQuery = true)
    List<IdHolder> findAllByViewIdsIn(Collection<Integer> viewIds, Integer totalCategories);

    @Query(value = """
        SELECT * FROM accommodations
        where
        	type in :types and
        	status in :statusList and
            (-1 in :accommodationIds or id in :accommodationIds)
        """, nativeQuery = true)
    Page<Accommodation> findAllThatMeet(List<String> types, List<String> statusList, Collection<Integer> accommodationIds, Pageable pageable);


    @Query(nativeQuery = true, value =
        """
            SELECT
                distinct rooms.accommodation_id as id
            FROM
                (
                    SELECT
                        rooms.id,
                        sum(details.total_rooms) AS rooms_left
                    FROM
                        rooms
                        INNER JOIN booking_requests_details details ON details.room_id = rooms.id
                        INNER JOIN booking_requests ON details.booking_request_id = booking_requests.id
                    WHERE
            			booking_requests.status NOT IN ('CANCELED', 'REJECTED')
                        AND (
                            (
                                booking_requests.checkin_at <= :fromDate
                                AND booking_requests.checkout_at <= :toDate
                            )
                            OR (
                                :fromDate <= booking_requests.checkin_at
                                AND booking_requests.checkout_at <= :toDate
                            )
                            OR (
                                :fromDate <= booking_requests.checkin_at
                                AND :toDate BETWEEN booking_requests.checkin_at
                                AND booking_requests.checkout_at
                            )
                            OR (
                                booking_requests.checkin_at <= :fromDate
                                AND :toDate <= booking_requests.checkout_at
                            )
                        )
                    GROUP BY
                        rooms.id
                ) AS booked_rooms
                RIGHT JOIN rooms ON booked_rooms.id = rooms.id
            WHERE
            	rooms.status = 'OPENING'
                AND (rooms.count - IFNULL(booked_rooms.rooms_left, 0)) >= :havingTotalRooms
                AND (-1 in :roomIds or rooms.id in :roomIds)
                AND (-1 in :accommodationIds or rooms.accommodation_id in :accommodationIds)
            """)
    List<IdHolder> findAllAccommodationsThatHavingRoomsMeet(Timestamp fromDate, Timestamp toDate, int havingTotalRooms, Collection<Integer> roomIds, Collection<Integer> accommodationIds);

    @Query(nativeQuery = true, value =
        """
            SELECT id
            FROM h2s.accommodations
            WHERE (
               3959 *
               acos(cos(radians(:latitude)) *\s
               cos(radians(latitude)) *\s
               cos(radians(longitude) -\s
               radians(:longitude)) +\s
               sin(radians(:latitude)) *\s
               sin(radians(latitude )))
            ) < :findingRange
                AND (-1 in :accommodationIds or id in :accommodationIds)
            """)
    List<IdHolder> findAccommodationsByCoordinateNear(double latitude, double longitude, double findingRange, Collection<Integer> accommodationIds);

}
