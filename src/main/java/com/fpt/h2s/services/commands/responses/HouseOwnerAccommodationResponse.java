package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Accommodation;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class HouseOwnerAccommodationResponse {
    private Integer id;
    private String name;
    private String thumbnail;
    private String shortDescription;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private Long minPrice;
    private Long maxPrice;
    private Accommodation.Type type;
    private Accommodation.Status status;
    private Integer totalRoom;
    private Integer totalViews;
    private Integer totalLikes;
    private Integer totalReviews;
    private Integer totalBookings;
    private Float reviewRate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    public static HouseOwnerAccommodationResponse of(final Accommodation accommodation) {
        return HouseOwnerAccommodationResponse
                .builder()
                .id(accommodation.getId())
                .name(accommodation.getName())
                .thumbnail(accommodation.getThumbnail())
                .shortDescription(accommodation.getShortDescription())
                .description(accommodation.getDescription())
                .address(accommodation.getAddress())
                .latitude(accommodation.getLatitude())
                .longitude(accommodation.getLongitude())
                .minPrice(accommodation.getMinPrice())
                .maxPrice(accommodation.getMaxPrice())
                .type(accommodation.getType())
                .status(accommodation.getStatus())
                .totalRoom(accommodation.getTotalRoom())
                .totalViews(accommodation.getTotalViews())
                .totalLikes(accommodation.getTotalLikes())
                .totalReviews(accommodation.getTotalReviews())
                .totalBookings(accommodation.getTotalBookings())
                .reviewRate(accommodation.getReviewRate())
                .createdAt(accommodation.getCreatedAt())
                .updatedAt(accommodation.getUpdatedAt())
                .deletedAt(accommodation.getDeletedAt())
                .build();
    }
}
