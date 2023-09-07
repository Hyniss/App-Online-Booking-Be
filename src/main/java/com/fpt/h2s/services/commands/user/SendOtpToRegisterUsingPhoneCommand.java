package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.OtpDetail;
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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class SendOtpToRegisterUsingPhoneCommand implements BaseCommand<SendOtpToRegisterUsingPhoneCommand.Request, String> {
    public static final Duration OTP_DURATION = Duration.ofMinutes(5);
    private final OtpRepository otpRepository;
    private final SmsService smsService;

    @Override
    public ApiResponse<String> execute(final Request request) {
        final String otpKey = StringEx.format("{}-{}-registerOTP", request.toPhone(), MoreRequests.getIPAddress());

        final String disableKey = StringEx.format("{}-Disabled", request.toPhone());
        if (RedisRepository.hasKey(disableKey)) {
            throw ApiException.badRequest("Tài khoản của bạn đã vượt quá số lần xác thực trong ngày. Xin hãy quay lại vào ngày mai.");
        }

        final String resendKey = StringEx.format("{}-resend", otpKey);

        if (RedisRepository.hasKey(resendKey)) {
            throw ApiException.badRequest("Xin hãy đợi 30 giây để gửi lại.");
        }

        final OtpDetail otpDetail = OtpUtils.findOtpByKey(otpKey).orElseGet(() -> createOtp(otpKey));
        this.smsService.sendTo(request.toPhone(), "OTP xác nhận của bạn là: {}", otpDetail.getValue());

        final OtpDetail savedOtp = this.otpRepository.save(otpDetail);
        log.info("Save otp succeed with id {} and value {}", savedOtp.getId(), savedOtp.getValue());

        RedisRepository.set(otpKey, savedOtp, Duration.ofMinutes(15));
        RedisRepository.set(resendKey, "", Duration.ofSeconds(30));

        return ApiResponse.success("Xin hãy sử dụng mã OTP chúng tôi gửi đến số điện thoại của bạn để xác nhận.", otpKey);
    }

    private static OtpDetail createOtp(final String key) {
        final Timestamp expiredAt = Timestamp.from(Instant.now().plus(OTP_DURATION));
        final String otp = OtpDetail.generate();
        return OtpDetail.builder()
            .otpKey(key)
            .value(otp)
            .ipAddress(MoreRequests.getIPAddress())
            .expiredAt(expiredAt)
            .build();
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {
        @NotBlank(message = "Xin hãy chọn quốc gia")
        @jakarta.validation.constraints.Pattern(regexp = "^(\\+?\\d{1,3}|\\d{1,4})$", message = "Mã quốc gia không hợp lệ")
        private final String region;

        @NotBlank(message = "{PHONE_BLANK}")
        @jakarta.validation.constraints.Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$", message = "Số điện thoại không hợp lệ")
        private final String phone;

        public String toPhone() {
            final String phone = this.phone.length() == 10 ? this.phone.substring(1) : this.phone;
            return StringEx.format("+{} {}", this.region, phone);
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

        private String validatePhone() {
            if (this.userRepository.existsByPhone(this.request.toPhone())) {
                return "Số điện thoại đã được sử dụng. Xin hãy sử dụng số điện thoại khác.";
            }
            return null;
        }

    }
}
