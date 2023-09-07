package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.OtpDetail;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.OtpRepository;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.OtpUtils;
import com.fpt.h2s.utilities.Tokens;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class ResetPasswordOTPVerificationCommand implements BaseCommand<ResetPasswordOTPVerificationCommand.ConfirmResetOTPRequest, String> {

    private final OtpRepository otpRepository;

    public static final Map<Integer, Integer> otpRetryMapToUserId = new HashMap<>();
    public static final int MAX_TRY = 5;

    @Override
    public ApiResponse<String> execute(final ConfirmResetOTPRequest request) {
        final String key = StringEx.format("{}-{}", request.id, MoreRequests.getIPAddress());

        final OtpDetail otp = OtpUtils
            .findOtpByKey(key)
                .orElseThrow(() -> ApiException.badRequest("Không tìm thấy mã OTP. Xin hãy gửi lại để lấy mã khác."));

        try {
            OtpUtils.validateOtp(otp, request.otp, MAX_TRY);
        } catch (final OtpUtils.WrongOtpException e) {
            throw ApiException.badRequest("Mã OTP không đúng. Bạn còn {} lần nhập.", e.getTryLeft());
        } catch (final OtpUtils.OtpDisabledException e) {
            final String disableKey = StringEx.format("{}-Disabled", request.id);
            RedisRepository.set(disableKey, "", Duration.ofDays(1));
            throw ApiException.badRequest("Tài khoản của bạn đã vượt quá số lần xác thực trong ngày. Xin hãy quay lại vào ngày mai.");
        }

        final OtpDetail otpToSave = otp.markVerified();
        this.otpRepository.save(otpToSave);

        final String token = Tokens.generateToken(
            Map.ofEntries(
                Map.entry("key", "register-phone-number"),
                Map.entry("ip", MoreRequests.getIPAddress()),
                Map.entry("otp", otp.getValue()),
                Map.entry("time", LocalDateTime.now().toString()),
                Map.entry("id", request.id)
            ),
            Duration.ofMinutes(30), "AJSGDBUAGD7Y2U8AX9A0D9AS8DFASID"
        );
        RedisRepository.set(token, request.id, Duration.ofMinutes(30));

        return ApiResponse.success("Xác nhận thành công", token);
    }


    @Getter
    @NoArgsConstructor
    @Setter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class ConfirmResetOTPRequest extends BaseRequest {
        @NotBlank(message = "Xin hãy nhập mã OTP.")
        private String otp;

        @NotNull
        private Integer id;
    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<ConfirmResetOTPRequest> {

        private final UserRepository userRepository;

        @Override
        protected void validate() {
            this.rejectIfEmpty(ConfirmResetOTPRequest.Fields.id, this::validateId);
        }

        private String validateId() {
            if (!this.userRepository.existsById(this.request.id)) {
                return "Không tìm thấy người dùng";
            }
            final User user = this.userRepository.findById(this.request.id).orElseThrow();
            if (user.is(User.Status.BANNED)) {
                return "Người dùng đã bị khóa";
            }
            return null;
        }

    }
}
