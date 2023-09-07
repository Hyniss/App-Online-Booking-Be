package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static com.fpt.h2s.models.entities.User.Role.HOUSE_OWNER;

@Log4j2
@Service
@RequiredArgsConstructor
public class ViewUserProfileCommand implements BaseCommand<ViewUserProfileCommand.Request, ViewUserProfileCommand.Response> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public ApiResponse<Response> execute(Request request) {
        User user = userRepository.getById(request.id, "User not found.");
        if (user.is(User.Status.BANNED)) {
            throw ApiException.forbidden("User is banned.");
        }

        UserProfile profile = getProfileOf(user);

        Response response = Response
            .builder()
            .id(user.getId())
            .username(user.getUsername())
            .isHost(user.is(HOUSE_OWNER))
            .reviews(profile.getTotalReview())
            .rating(profile.getTotalRate())
            .avatar(profile.getAvatar())
            .address(profile.getAddress())
            .joinedAt(user.getCreatedAt())
            .bio(profile.getBio())
            .phone(user.getPhone())
            .email(user.getEmail())
            .waiting(RedisRepository.get("update-email-%s".formatted(user.getId())).orElse(null))
            .password(user.getPassword() != null)
            .build();

        return ApiResponse.success(response);
    }

    private UserProfile getProfileOf(User user) {
        if (user.getUserProfile() != null) {
            return user.getUserProfile();
        }
        UserProfile profile = UserProfile.builder().userId(user.getId()).build();
        return userProfileRepository.save(profile);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotNull
        private final Integer id;
    }
    @Getter
    @Builder
    @With
    @FieldNameConstants
    @Jacksonized
    public static class Response {

        private final Integer id;
        private final String username;
        private final String phone;
        private final String email;
        private final String avatar;
        private final boolean isHost;
        private final String waiting;
        private final boolean password;
        private final Double rating;
        private final Integer reviews;
        private final String address;
        private final String bio;
        private final Timestamp joinedAt;
    }

}