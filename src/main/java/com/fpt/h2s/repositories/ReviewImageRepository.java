package com.fpt.h2s.repositories;

import com.fpt.h2s.models.entities.ReviewImage;

import java.util.Collection;
import java.util.List;

public interface ReviewImageRepository extends BaseRepository<ReviewImage, Integer> {
    List<ReviewImage> findAllByReviewIdIn(Collection<Integer> ids);

    List<ReviewImage> findAllByReviewId(Integer id);
}