package com.fpt.h2s.models.domains;

import com.fpt.h2s.models.entities.User;
import lombok.Builder;

import java.util.List;

@Builder
public record TokenUser(Integer id, String email, String username, String phone, List<User.Role> scopes) {

    public static TokenUser of(final User user) {
        return TokenUser
            .builder()
            .id(user.getId())
            .email(user.getUsername())
            .username(user.getUsername())
            .phone(user.getPhone())
            .scopes(user.roleList())
            .build();
    }
}
