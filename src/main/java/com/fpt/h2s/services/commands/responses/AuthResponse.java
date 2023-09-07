package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.With;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@With
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    
    private Integer id;
    private String email;
    private String username;
    private String phone;
    private String avatar;
    private List<User.Role> roles;
    
    private String accessToken;
    
    public static AuthResponse from(@NonNull final User user, @NonNull final String token) {
        return from(user, user.getUserProfile(), token);
    }

    public static AuthResponse from(@NonNull final User user, UserProfile profile, @NonNull final String token) {
        return AuthResponse.builder()
            .id(user.getId())
            .avatar(Optional.ofNullable(profile).map(UserProfile::getAvatar).orElse(null))
            .email(user.getEmail())
            .username(user.getUsername())
            .phone(user.getPhone())
            .roles(user.roleList())
            .accessToken(token)
            .build();
    }
}
