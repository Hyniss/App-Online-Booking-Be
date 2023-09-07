package com.fpt.h2s.services.commands.responses;


import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Category;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class HouseOwnerDetailAccommodationResponse {
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

    private Set<DetailAccommodationSetImageResponse> images;

    private Set<Category> location;

    private Set<HouseOwnerDetailAccommodationRoomResponse> rooms;

    public static HouseOwnerDetailAccommodationResponse of(Accommodation accommodation,
                                                           Set<DetailAccommodationSetImageResponse> images,
                                                           Set<Category> categories,
                                                           Set<HouseOwnerDetailAccommodationRoomResponse> rooms) {
        return HouseOwnerDetailAccommodationResponse
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
                .location(categories)
                .rooms(rooms)
                .build();
    }
}
