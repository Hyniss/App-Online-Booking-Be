package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.User;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
public class UserDetailResponse {
    private Integer id;

    private String email;

    private String username;

    private String phone;

    private List<User.Role> roles;

    private User.Status status;

    private Integer creatorId;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private String avatar;

    private List<SpendingResponse> spendingResponses;

    public static UserDetailResponse of(User user, List<SpendingResponse> spendingResponses) {
        return UserDetailResponse
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .phone(user.getPhone())
                .roles(user.roleList())
                .status(user.getStatus())
                .creatorId(user.getCreatorId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .avatar((user.getUserProfile() == null) ? null : user.getUserProfile().getAvatar())
                .spendingResponses(spendingResponses)
                .build();
    }
}
