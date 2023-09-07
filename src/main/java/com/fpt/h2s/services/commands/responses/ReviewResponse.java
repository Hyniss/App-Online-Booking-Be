package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.services.commands.accommodation.CheckAccommodationReviewableCommand.BookingDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
@With
@AllArgsConstructor
public class ReviewResponse {
    private Integer id;

    private UserResponse owner;

    private String content;

    private Integer rate;

    private boolean canEdit;

    private boolean canDelete;

    private Timestamp createdAt;

    private List<String> images;

    private BookingDetailResponse detail;

}
