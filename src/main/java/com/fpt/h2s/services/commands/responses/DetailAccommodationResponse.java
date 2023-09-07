package com.fpt.h2s.services.commands.responses;


import com.fpt.h2s.models.entities.*;
import lombok.Builder;
import lombok.Getter;
import java.util.Set;

@Builder
@Getter
public class DetailAccommodationResponse {

    private Integer id;

    private String name;

    private String thumbnail;

    private String shortDescription;

    private String description;

    private String address;

    private Double latitude;

    private Double longtiude;

    private Long minPrice;

    private Long maxPrice;

    private Accommodation.Type type;

    private Integer totalRoom;

    private Integer totalViews;

    private Integer totalLikes;

    private Integer totalReviews;

    private Float reviewRate;

    private Integer ownerId;

    private String username;

    private String email;

    private String phone;

    private String avatar;

    private Integer duration;

    private Set<AccommodationImage> images;

    private Set<Category> categories;

    private Set<DetailAccommodationRoomResponse> rooms;

    public static DetailAccommodationResponse of(Accommodation accommodation,
                                                 Set<AccommodationImage> images,
                                                 Set<Category> categories,
                                                 Set<DetailAccommodationRoomResponse> rooms,
                                                 Integer duration) {
        return DetailAccommodationResponse
                .builder()
                .id(accommodation.getId())
                .name(accommodation.getName())
                .thumbnail(accommodation.getThumbnail())
                .shortDescription(accommodation.getShortDescription())
                .description(accommodation.getDescription())
                .address(accommodation.getAddress())
                .latitude(accommodation.getLatitude())
                .longtiude(accommodation.getLongitude())
                .minPrice(accommodation.getMinPrice())
                .maxPrice(accommodation.getMaxPrice())
                .type(accommodation.getType())
                .totalRoom(accommodation.getTotalRoom())
                .totalViews(accommodation.getTotalViews())
                .totalLikes(accommodation.getTotalLikes())
                .totalReviews(accommodation.getTotalReviews())
                .reviewRate(accommodation.getReviewRate())
                .ownerId(accommodation.getOwnerId())
                .username(accommodation.getOwner().getUsername())
                .email(accommodation.getOwner().getEmail())
                .phone(accommodation.getOwner().getPhone())
                .avatar((accommodation.getOwner().getUserProfile() == null)
                        ? null : accommodation.getOwner().getUserProfile().getAvatar())
                .images(images)
                .categories(categories)
                .rooms(rooms)
                .duration(duration)
                .build();
    }

}
