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
import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.NotNull;
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
public class VerifyUpdateUserEmailCommand implements BaseCommand<VerifyUpdateUserEmailCommand.Request, AuthResponse> {

    public static final String KEY = "sdh8132871insuah9hd8y9312h21h01ja9dshasd9g71279982e98as";
    private final UserRepository userRepository;

    @Override
    public ApiResponse<AuthResponse> execute(Request request) {
        RedisRepository.get(request.code).orElseThrow(() -> ApiException.badRequest("Phiên cập nhật đã hết, xin hãy gửi mã OTP mới."));

        Claims claims = Tokens.getClaimOf(request.code, KEY);
        int userId = (int) claims.get("userId");
        User user = userRepository.getById(userId);

        String email = String.valueOf(claims.get("email"));
        User savedUser = userRepository.save(user.withEmail(email));

        log.info("Updated email for user {}", userId);

        final String token = AuthUtils.getGeneratedTokenOf(savedUser);
        final AuthResponse response = AuthResponse.from(savedUser, token);

        String oldToken = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());

        RedisRepository.remove(request.code);
        RedisRepository.remove(oldToken);
        RedisRepository.remove("update-email-%s".formatted(user.getId()));

        return ApiResponse.success("Cập nhật email thành công.", response);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotNull
        private final String code;
    }

}