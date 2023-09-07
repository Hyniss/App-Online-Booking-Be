package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Accommodation;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class AdminAccommodationResponse {
    private Integer id;
    
    private String name;
    
    private Integer ownerId;
    
    private String ownerName;
    
    private String thumbnail;
    
    private String address;
    
    private Long minPrice;
    
    private Long maxPrice;
    
    private Accommodation.Type type;
    
    private Accommodation.Status status;
    
    private Integer totalRoom;
    
    private Timestamp createdAt;
    
    
    public static AdminAccommodationResponse of(final Accommodation accommodation) {
        return AdminAccommodationResponse
            .builder()
            .id(accommodation.getId())
            .name(accommodation.getName())
            .ownerId(accommodation.getOwnerId())
            .ownerName(accommodation.getOwner().getUsername())
            .thumbnail(accommodation.getThumbnail())
            .address(accommodation.getAddress())
            .minPrice(accommodation.getMinPrice())
            .maxPrice(accommodation.getMaxPrice())
            .type(accommodation.getType())
            .status(accommodation.getStatus())
            .totalRoom(accommodation.getTotalRoom())
            .createdAt(accommodation.getCreatedAt())
            .build();
    }
}
