package com.fpt.h2s.services.commands.responses;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.utilities.Mappers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@FieldNameConstants
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
@With
public class AccommodationResponse {
    private Integer id;
    private String name;
    private List<String> thumbnails;
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
    private Integer creatorId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean liked;
    private UserResponse owner;
    
    public static AccommodationResponse of(final Accommodation accommodation) {
        return Mappers.convertTo(AccommodationResponse.class, accommodation);
    }
}