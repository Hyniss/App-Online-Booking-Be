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
public class RemoveNotificationTokenDeviceForUserCommand implements BaseCommand<RemoveNotificationTokenDeviceForUserCommand.Request, Void> {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    @Override
    public ApiResponse<Void> execute(final Request request) {
        final Integer userId = User.getCurrentId();
        userDeviceTokenRepository.findByUserIdAndToken(userId, request.token).ifPresent(userDeviceTokenRepository::delete);
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
