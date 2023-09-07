package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.PriceHistory;
import com.fpt.h2s.repositories.projections.PriceHistoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    @Query(nativeQuery = true, value = """
            select
            	id,
            	roomId,
            	type,
            	date as dayType,
            	fromDate,
            	toDate,
            	amount,
            	case
            		when type = 'discount' then ((price * ((select ph1.amount from price_histories ph1 where ph1.room_id  = roomId and ph1.day_type = date and ph1.type = 'PRICE') / 100)) * ((100 - amount) / 100))
            		else price
            	end as price
            from
            	(
            	select
            		id,
            		room_id as roomId,
            		type,
            		day_type as date,
            		from_date as fromDate,
            		to_date as toDate,
            		amount,
            		price
            from
            		(
            	select
            			ph.id,
            			ph.room_id,
            			ph.`type`,
            			ph.day_type,
            			case
            				when ph.to_date is null then null
            			else ph.from_date
            		end as from_date,
            			case
            				when ph.to_date is not null then ph.to_date
            			else LAST_DAY(DATE_ADD(MAKEDATE(year(:date), 1), interval 1 year) - interval 1 day)
            		end as to_date,
            			ph.amount,
            		case
            			when ph.day_type in ('WEEKDAY', 'WEEKEND', 'SPECIAL_DAY')
            			and ph.type = 'PRICE' then (r.price * ((ph.amount) / 100))
            			else r.price
            		end as price
            	from
            			price_histories ph
            	join rooms r on
            			ph.room_id = r.id
            	join accommodations a on
            			r.accommodation_id = a.id
            	where
            			a.id = :accommodationId
            		and (ph.to_date is null
            			or ph.to_date >= :date)
            	order by
            			room_id,
            			type,
            			case
            				day_type
                        when 'WEEKDAY' then 1
            			when 'WEEKEND' then 2
            			when 'SPECIAL_DAY' then 3
            			else 4
            		end
                        ) as selected_price
            where
            		from_date is null
            	or (from_date is not null
            		and (from_date,
            			day_type,
            			room_id) in
                        	(
            		select
            				min(from_date),
            				day_type,
            				room_id
            		from
            				(
            			select
            					ph.day_type,
            					ph.room_id,
            					case
            						when ph.from_date < :date then null
            					else ph.from_date
            				end as from_date,
            					DATEDIFF(ph.from_date, :date) as date_diff
            			from
            					price_histories ph
            			join rooms r on
            					ph.room_id = r.id
            			join accommodations a on
            					r.accommodation_id = a.id
            			where
            					a.id = :accommodationId
            				and (ph.to_date is not null
            					or ph.to_date >= :date)
            			order by
            					case
            						ph.day_type
                        			when 'WEEKDAY' then 1
            					when 'WEEKEND' then 2
            					when 'SPECIAL_DAY' then 3
            					else 4
            				end,
            					ph.from_date
                        		) as selected_price
            		where
            				from_date is not null
            			and from_date > :date
            		group by
            				day_type,
            				room_id
                        	))) as final_price""")
    List<PriceHistoryType> getAmountByDate(@Param("accommodationId") Integer accommodationId,
                                           @Param("date") LocalDate date);

    List<PriceHistory> findByRoomIdAndTypeAndDayType(Integer roomId, PriceHistory.Type type, PriceHistory.DayType date);

    List<PriceHistory> findByRoomIdAndTypeAndDayTypeAndFromDate(Integer roomId, PriceHistory.Type type, PriceHistory.DayType date, LocalDate fromDate);

    List<PriceHistory> findByRoomIdAndTypeAndDayTypeAndToDate(Integer roomId, PriceHistory.Type type, PriceHistory.DayType date, LocalDate toDate);


    @Query(nativeQuery = true, value = """
            WITH RECURSIVE date_range AS (
              SELECT :checkInDate AS price_date
              UNION ALL
              SELECT DATE_ADD(price_date, INTERVAL 1 DAY)
              FROM date_range
              WHERE DATE_ADD(price_date, INTERVAL 1 DAY) < :checkOutDate
            ),
            latest_prices AS (
              SELECT
                ph.*,
                ROW_NUMBER() OVER (PARTITION BY ph.room_id, dr.price_date ORDER BY ph.from_date DESC, ph.type DESC) AS rn,
                dr.price_date
              FROM price_histories ph
              JOIN date_range dr ON ph.from_date <= dr.price_date
                                 AND (ph.to_date >= dr.price_date OR ph.to_date IS NULL)
              WHERE ph.room_id IN (
                SELECT DISTINCT room_id
                FROM price_histories
                WHERE from_date <= :checkOutDate
              )
            ),
            latest_discounts AS (
              SELECT
                lp.price_date,
                lp.room_id,
                MAX(lp.amount) AS discount
              FROM latest_prices lp
              WHERE lp.type = 'discount'
              GROUP BY lp.price_date, lp.room_id
            )
            SELECT
              dr.price_date,
              COALESCE(ld.discount, 0) AS discount,
              COALESCE(MAX(CASE WHEN lp.type = 'price' THEN lp.amount END), 0) AS price
            FROM date_range dr
            CROSS JOIN rooms r
            LEFT JOIN latest_prices lp ON dr.price_date = lp.price_date
                                       AND lp.rn = 1
                                       AND lp.room_id = r.id
            LEFT JOIN latest_discounts ld ON dr.price_date = ld.price_date
                                           AND lp.room_id = ld.room_id
            WHERE r.id = :roomId
            GROUP BY dr.price_date
            ORDER BY dr.price_date
            """)
    List<Object[]> getAmountByCheckInCheckOutDate(@Param("roomId") Integer roomId,
                                                  @Param("checkInDate") LocalDate checkInDate,
                                                  @Param("checkOutDate") LocalDate checkOutDate);

    @Query(nativeQuery = true, value = """
            WITH room_prices AS (
                (
                    WITH custom_dates AS (
                        SELECT dates.date
                        FROM
                            price_histories price
                            JOIN dates ON date BETWEEN price.from_date AND price.to_date
                        WHERE
                            day_type = 'CUSTOM' AND room_id IN :roomIds
                    )
                    SELECT room_id, amount, price.type, price.day_type AS date_type, dates.date AS value
                    FROM
                        price_histories price
                        INNER JOIN dates ON price.day_type = dates.type
                        LEFT JOIN custom_dates ON dates.date = custom_dates.date
                    WHERE
                        room_id IN :roomIds AND custom_dates.date IS NULL AND dates.date BETWEEN :fromDate AND :toDate
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
                        AND room_id IN :roomIds
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
            """)
    List<Map<String, Object>> findPricesOf(Collection<Integer> roomIds, Timestamp fromDate, Timestamp toDate);

    @Query(nativeQuery = true, value = """
            SELECT
            	prices.room_id,
            	prices.day_type,
            	(rooms.price * prices.amount / 100) AS price,
            	IFNULL(discounts.amount, 0) AS discount,
            	(rooms.price * prices.amount / 100) * (100 - IFNULL(discounts.amount, 0)) / 100 AS display_price
            FROM (
            	SELECT *
            	FROM price_histories
            	WHERE
            		room_id IN :roomIds AND
            		type = 'PRICE' AND
                    day_type != 'CUSTOM'
            ) AS prices
            LEFT JOIN (
            	SELECT *
            	FROM price_histories
            	WHERE
            		room_id IN :roomIds AND
            		type = 'DISCOUNT' AND
                    day_type != 'CUSTOM'
            ) AS discounts ON discounts.room_id = prices.room_id AND discounts.day_type = prices.day_type
            INNER JOIN rooms on prices.room_id = rooms.id
            """)
    List<Map<String, Object>> findNonCustomPricesOfRooms(Collection<Integer> roomIds);
}
