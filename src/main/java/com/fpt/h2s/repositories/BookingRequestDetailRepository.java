package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.BookingRequestDetail;
import com.fpt.h2s.services.commands.accommodation.CheckAccommodationReviewableCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BookingRequestDetailRepository extends JpaRepository<BookingRequestDetail, Integer> {

    @Query(nativeQuery = true, value = """
            SELECT booking_requests.id, rooms.name, booking_requests.updated_at as bookedAt
            FROM h2s.booking_requests_details
            inner join booking_requests on booking_requests.id = booking_requests_details.booking_request_id
            inner join rooms on booking_requests_details.room_id = rooms.id
            where booking_requests.id in :ids
        """)
    List<CheckAccommodationReviewableCommand.BookingRoomProjection> findAllByBookingRequestIdIn(Collection<Integer> ids);

    @Query(nativeQuery = true, value = """
            SELECT accommodations_reviews.id, rooms.name, booking_requests.updated_at as bookedAt
            from accommodations_reviews
            INNER JOIN booking_requests on booking_requests.id = accommodations_reviews.request_id
            inner join booking_requests_details on booking_requests.id = booking_requests_details.booking_request_id
            inner join rooms on booking_requests_details.room_id = rooms.id
            WHERE accommodations_reviews.id in :reviewIds
        """)

    List<CheckAccommodationReviewableCommand.BookingRoomProjection> findAllByReviews(Collection<Integer> reviewIds);
}
