package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.User;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class BusinessAdminSearchBusinessMemberResponse {
    private Integer id;

    private String email;

    private String phone;

    private String username;

    private String roles;

    private User.Status status;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    public static BusinessAdminSearchBusinessMemberResponse of(final User user) {
        return BusinessAdminSearchBusinessMemberResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .roles(user.getRoles())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }

}
