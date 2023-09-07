package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.AccommodationReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccommodationReviewRepository extends BaseRepository<AccommodationReview, Integer> {
    Page<AccommodationReview> findAllByAccommodationId(Integer id, Pageable pageable);

    @Query(nativeQuery = true, value = """
        select rate from accommodations_reviews
        inner join accommodations on accommodation_id = accommodations.id
        where accommodations.creator_id = :id
        """)
    List<Integer> findAllRatesOfUser(Integer id);

    @Query(nativeQuery = true, value = """
        SELECT reviews.*
        FROM accommodations_reviews reviews
        INNER JOIN accommodations on accommodations.id = reviews.accommodation_id
        WHERE accommodations.owner_id = :id
        """, countQuery = """
         SELECT COUNT(*)
        FROM accommodations_reviews reviews
        INNER JOIN accommodations on accommodations.id = reviews.accommodation_id
        WHERE accommodations.owner_id = :id
        """)
    Page<AccommodationReview> findAllByOwnerId(Integer id, Pageable pageable);

    boolean existsByRequestId(Integer id);

}
