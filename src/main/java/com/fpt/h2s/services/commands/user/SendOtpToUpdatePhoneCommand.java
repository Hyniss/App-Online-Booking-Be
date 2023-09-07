package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.OtpDetail;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.OtpRepository;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.SmsService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.OtpUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class SendOtpToUpdatePhoneCommand implements BaseCommand<SendOtpToUpdatePhoneCommand.Request, String> {
    public static final Duration OTP_DURATION = Duration.ofMinutes(5);
    private final SmsService smsService;
    private final OtpRepository otpRepository;

    @Override
    public ApiResponse<String> execute(final Request request) {
        Integer userId = User.getCurrentId();
        final String otpKey = StringEx.format("{}-{}-updatePasswordOTP", userId, MoreRequests.getIPAddress());

        final String disableKey = StringEx.format("{}-Disabled", otpKey);
        if (RedisRepository.hasKey(disableKey)) {
            throw ApiException.badRequest("Tài khoản của bạn đã vượt quá số lần xác thực trong ngày. Xin hãy quay lại vào ngày mai.");
        }

        final String resendKey = StringEx.format("{}-resend", otpKey);

        if (RedisRepository.hasKey(resendKey)) {
            throw ApiException.badRequest("Xin hãy đợi 30 giây để gửi lại.");
        }

        final OtpDetail otpDetail = OtpUtils.findOtpByKey(otpKey).orElseGet(() -> createOtp(otpKey));
        smsService.sendTo("+84 " + request.toPhone(), "OTP xác nhận của bạn là: {}", otpDetail.getValue());

        final OtpDetail savedOtp = this.otpRepository.save(otpDetail);
        log.info("Save otp succeed with id {} and value {}", savedOtp.getId(), savedOtp.getValue());

        RedisRepository.set(otpKey, savedOtp, Duration.ofMinutes(15));
        RedisRepository.set(resendKey, "", Duration.ofSeconds(30));

        return ApiResponse.success("Xin hãy sử dụng mã OTP chúng tôi gửi đến số điện thoại của bạn để xác nhận.", otpKey);
    }

    private static OtpDetail createOtp(final String key) {
        return OtpDetail.builder()
            .otpKey(key)
            .value(OtpDetail.generate())
            .ipAddress(MoreRequests.getIPAddress())
            .expiredAt(Timestamp.from(Instant.now().plus(OTP_DURATION)))
            .build();
    }


    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {
        @NotBlank(message = "Xin hãy nhập email hoặc số điện thoại")
        private String phone;

        @NotNull
        private String toPhone() {
            if (StringEx.lengthOf(phone) == 10) {
                return this.phone.substring(1);
            }
            return this.phone;
        }

    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<Request> {

        private final UserRepository userRepository;

        @Override
        protected void validate() {
            this.rejectIfEmpty(Request.Fields.phone, this::validatePhone);
        }

        @Nullable
        private String validatePhone() {
            final String phone = request.toPhone();
            if (!phone.matches("^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$")) {
                return "Số điện thoại không hợp lệ.";
            }
            this.userRepository
                .findByPhoneEndingWith("%" + phone)
                .ifPresent((u) -> {
                    throw ApiException.badRequest("Số điện thoại đã được sử dụng.");
                });

            final Integer totalTried = ResetPasswordOTPVerificationCommand.otpRetryMapToUserId.getOrDefault(User.getCurrentId(), 0);
            if (totalTried >= ResetPasswordOTPVerificationCommand.MAX_TRY) {
                throw ApiException.badRequest("Tài khoản của bạn hiện không thể khôi phục mật khẩu.");
            }
            return null;
        }

    }
}
