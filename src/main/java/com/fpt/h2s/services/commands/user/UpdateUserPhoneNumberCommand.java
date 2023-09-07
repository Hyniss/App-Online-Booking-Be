package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.AuthResponse;
import com.fpt.h2s.services.commands.user.utils.AuthUtils;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.Tokens;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UpdateUserPhoneNumberCommand implements BaseCommand<UpdateUserPhoneNumberCommand.Request, AuthResponse> {

    private final UserRepository userRepository;

    @Override
    public ApiResponse<AuthResponse> execute(Request request) {
        final String redisToken = request.getToken();
        final String phoneNumber = RedisRepository
            .get(redisToken)
            .orElseThrow(() -> ApiException.badRequest("Có lỗi xảy ra, xin hãy tải lại trang để tiếp tục."));

        Integer userId = User.currentUserId().orElseThrow();
        User user = userRepository.getById(userId);
        User savedUser = userRepository.save(user.withPhone(phoneNumber));

        final String token = AuthUtils.getGeneratedTokenOf(savedUser);
        final AuthResponse response = AuthResponse.from(savedUser, token);

        String oldToken = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());
        RedisRepository.remove(oldToken);

        log.info("Update user phone number for {} using phone {}", userId, phoneNumber);
        return ApiResponse.success("Cập nhật số điện thoại thành công", response);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotBlank
        private final String token;
    }

}
