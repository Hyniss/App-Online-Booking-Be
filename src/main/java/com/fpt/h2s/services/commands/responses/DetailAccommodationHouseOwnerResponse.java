package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class DetailAccommodationHouseOwnerResponse {
    private Integer id;
    private String email;
    private String phone;
    private String username;
    private String avatar;
    private Integer totalReview;
    private Double totalRate;
    private Timestamp createdAt;

    public static DetailAccommodationHouseOwnerResponse of(final Accommodation accommodation, final Contract contract) {
        final User owner = accommodation.getOwner();
        return DetailAccommodationHouseOwnerResponse.builder()
            .id(owner.getId())
            .email(owner.getEmail())
            .phone(owner.getPhone())
            .username(owner.getUsername())
            .avatar(owner.getUserProfile() == null ? null : owner.getUserProfile().getAvatar())
            .totalReview(owner.getUserProfile() == null ? null : owner.getUserProfile().getTotalReview())
            .totalRate(owner.getUserProfile() == null ? null : owner.getUserProfile().getTotalRate())
            .createdAt(contract.getCreatedAt())
            .build();
    }
}
