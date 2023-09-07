package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.BookingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BookingRequestRepository extends BaseRepository<BookingRequest, Integer> {
    Page<BookingRequest> findAll(Specification<BookingRequest> specification, Pageable pageable);

    @Query(nativeQuery = true, value = """
        WITH RECURSIVE date_range AS (
          SELECT DATE_ADD(DATE(:givenMonth), INTERVAL 0 DAY) AS booking_date
          UNION ALL
          SELECT DATE_ADD(booking_date, INTERVAL 1 DAY)
          FROM date_range
          WHERE DATE_ADD(booking_date, INTERVAL 1 DAY) <= DATE_ADD(:givenMonth, INTERVAL 12 MONTH)
        )
        SELECT REMAIN_ROOM.booking_dates,
               REMAIN_ROOM.remaining_rooms
        FROM (
          SELECT DATE(DATE_RANGE.booking_date) AS booking_dates,
                 COALESCE(SUM(Booking_requests_specific.total_rooms), 0) AS remaining_rooms
          FROM date_range DATE_RANGE
          LEFT JOIN booking_requests Booking ON DATE(DATE_RANGE.booking_date) >= DATE(Booking.checkin_at)
          LEFT JOIN booking_requests_details Booking_requests_specific ON Booking.id = Booking_requests_specific.booking_request_id
            AND DATE(DATE_RANGE.booking_date) < DATE(Booking.checkout_at) AND Booking_requests_specific.room_id = :roomId
          WHERE Booking.status IN ('APPROVED', 'SUCCEED')
          GROUP BY DATE_RANGE.booking_date
          ORDER BY DATE_RANGE.booking_date
        ) AS REMAIN_ROOM;
        """)
    List<Object[]> getBookingResultsForRoomId(@Param("givenMonth") Timestamp startDate, @Param("roomId") int roomId);

    @Query(nativeQuery = true, value = """
        SELECT booking_requests.* from booking_requests
        LEFT JOIN accommodations_reviews on booking_requests.id = accommodations_reviews.request_id
        where
        	booking_requests.user_id = :userId and accommodations_reviews.content is null
        	and booking_requests.updated_at >= :longAgo
        order by booking_requests.updated_at desc
        """)
    List<BookingRequest> getUnReviewedBookingRequests(Integer userId, Timestamp longAgo);

    List<BookingRequest> findAllByStatusIn(Collection<BookingRequest.Status> status);

    @Query(nativeQuery = true, value = """
        select COUNT(1)
        from booking_requests
        where status in ('SUCCEED') and created_at >= :time
        """)
    Integer countSuccessRequestsSince(Timestamp time);

    @Query(nativeQuery = true, value = """
        select sum(transactions.amount) * 15 / 100
        from booking_requests
        INNER JOIN transactions
        ON booking_requests.transaction_id = transactions.id
        where status in ('SUCCEED') AND booking_requests.created_at >= :time
        """)
    Long getTotalRevenueSince(Timestamp time);

    @Query(nativeQuery = true, value = """
        select sum(transactions.amount) * 15 / 100 as amount,  YEAR(booking_requests.created_at) as year, MONTH(booking_requests.created_at) as month
        from booking_requests
        INNER JOIN transactions
        ON booking_requests.transaction_id = transactions.id
        where status in ('SUCCEED') AND TIMESTAMPDIFF(MONTH, booking_requests.created_at, CURRENT_DATE) < :months
        GROUP BY YEAR(booking_requests.created_at), MONTH(booking_requests.created_at)
        ORDER BY year, month
        """)
    List<Map<String, Object>> getRevenuesOfLastNMonths(Integer months);

    @Query(nativeQuery = true, value = """
         WITH RECURSIVE date_range AS (
          SELECT DATE_ADD(DATE(:checkInDate), INTERVAL 0 DAY) AS booking_date
          UNION ALL
          SELECT DATE_ADD(booking_date, INTERVAL 1 DAY)
          FROM date_range
          WHERE DATE_ADD(booking_date, INTERVAL 1 DAY) <= DATE(:checkOutDate)
        )
        SELECT MIN(REMAIN_ROOM.remaining_rooms) AS min_remaining_rooms,
               MIN(TIMESTAMP(REMAIN_ROOM.booking_dates)) AS start_date,
               MAX(TIMESTAMP(REMAIN_ROOM.booking_dates)) AS end_date
        FROM (
          SELECT DATE_RANGE.booking_date AS booking_dates,
            ((SELECT COUNT FROM rooms WHERE id = :roomId) - COALESCE(SUM(Booking_requests_specific.total_rooms), 0)) AS remaining_rooms
          FROM date_range DATE_RANGE
          LEFT JOIN booking_requests Booking ON DATE(DATE_RANGE.booking_date) >= DATE(Booking.checkin_at)
          LEFT JOIN booking_requests_details Booking_requests_specific ON Booking.id = Booking_requests_specific.booking_request_id
            AND DATE(DATE_RANGE.booking_date) < DATE(Booking.checkout_at) AND Booking_requests_specific.room_id = :roomId
          WHERE Booking.status IN ('APPROVED', 'SUCCEED')
          GROUP BY DATE_RANGE.booking_date
          HAVING remaining_rooms < :totalRooms
          ORDER BY DATE_RANGE.booking_date
        ) AS REMAIN_ROOM;
        """)
    Object[][] getBookingResultsByRoomIdCheckInCheckOut(@Param("roomId") int roomId,
                                                        @Param("checkInDate") Timestamp checkIn,
                                                        @Param("checkOutDate") Timestamp checkOut,
                                                        @Param("totalRooms") int totalRoomBook);


}
