package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Builder
@Getter
@With
public class UserResponse {
    private Integer id;

    private String email;

    private String username;
    private String avatar;

    private String phone;

    private List<User.Role> roles;

    private User.Status status;
    
    private Timestamp createdAt;

    private Timestamp updatedAt;

    public static UserResponse of(final User user) {
        UserProfile userProfile = user.getUserProfile();
        return Mappers.convertTo(UserResponse.class, user)
            .withAvatar(Optional.ofNullable(userProfile).map(UserProfile::getAvatar).orElse(null))
            .withRoles(user.roleList());
    }

}
