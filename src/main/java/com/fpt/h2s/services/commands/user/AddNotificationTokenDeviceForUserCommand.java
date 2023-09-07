package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserDeviceToken;
import com.fpt.h2s.repositories.UserDeviceTokenRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


@Log4j2
@Service
@AllArgsConstructor
public class AddNotificationTokenDeviceForUserCommand implements BaseCommand<AddNotificationTokenDeviceForUserCommand.Request, Void> {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    @Override
    public ApiResponse<Void> execute(final Request request) {
        final Integer userId = User.currentUserId().orElseThrow();
        final UserDeviceToken.PK id = UserDeviceToken.PK.builder().userId(userId).token(request.token).build();
        final UserDeviceToken userDeviceToken = UserDeviceToken.builder().id(id).build();
        try {
            this.userDeviceTokenRepository.save(userDeviceToken);
        } catch (final Exception ignored) {
            log.warn("Device token already existed.");
        }
        return ApiResponse.success();
    }
    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request {

        @NotBlank
        private final String token;
    }
}
