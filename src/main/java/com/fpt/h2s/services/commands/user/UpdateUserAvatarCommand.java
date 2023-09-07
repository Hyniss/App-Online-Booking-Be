package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.AmazonS3Service;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.AuthResponse;
import com.fpt.h2s.services.commands.user.utils.AuthUtils;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.Tokens;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class UpdateUserAvatarCommand implements BaseCommand<UpdateUserAvatarCommand.Request, AuthResponse> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AmazonS3Service amazonS3Service;

    @Override
    public ApiResponse<AuthResponse> execute(Request request) {
        User user = userRepository.getById(User.getCurrentId());
        UserProfile profile = updateProfile(request, user);
        final String token = AuthUtils.getGeneratedTokenOf(user);
        final AuthResponse response = AuthResponse.from(user, profile, token);

        String oldToken = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());
        RedisRepository.remove(oldToken);

        log.info("Update user avatar for {} using with value {}", user.getId(), request.getAvatar());
        return ApiResponse.success("Cập nhật ảnh đại diện thành công", response);
    }

    private UserProfile updateProfile(Request request, User user) {
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            UserProfile newProfile = UserProfile.builder().userId(user.getId()).avatar(request.avatar).build();
            return userProfileRepository.save(newProfile);
        }

        String oldAvatar = profile.getAvatar();

        if (oldAvatar != null) {
            if (Objects.equals(oldAvatar, request.avatar)) {
                throw ApiException.badRequest("Xin hãy chọn ảnh khác");
            }
            amazonS3Service.deleteFile(oldAvatar);
            log.info("Delete old avatar for user {} with url {}", user.getId(), oldAvatar);
        }
        UserProfile userProfile = profile.withAvatar(request.avatar);
        return userProfileRepository.save(userProfile);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotNull
        private final String avatar;
    }

}