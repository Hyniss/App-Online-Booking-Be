package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fpt.h2s.configurations.requests.DataContext;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.EmailTemplate;
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
import com.fpt.h2s.workers.MailWorker;
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

import static com.fpt.h2s.models.entities.User.Status.BANNED;

@Log4j2
@Service
@RequiredArgsConstructor
public class SendOtpToResetPasswordCommand implements BaseCommand<SendOtpToResetPasswordCommand.SendOTPResetPasswordRequest, String> {
    public static final Duration OTP_DURATION = Duration.ofMinutes(5);
    private final MailWorker mailWorker;
    private final SmsService smsService;
    private final OtpRepository otpRepository;

    @Override
    public ApiResponse<String> execute(final SendOTPResetPasswordRequest request) {
        final User user = DataContext.get(User.class);
        final String otpKey = StringEx.format("{}-{}-resetPasswordOTP", user.getId(), MoreRequests.getIPAddress());

        final String disableKey = StringEx.format("{}-Disabled", otpKey);
        if (RedisRepository.hasKey(disableKey)) {
            throw ApiException.badRequest("Tài khoản của bạn đã vượt quá số lần xác thực trong ngày. Xin hãy quay lại vào ngày mai.");
        }

        final String resendKey = StringEx.format("{}-resend", otpKey);

        if (RedisRepository.hasKey(resendKey)) {
            throw ApiException.badRequest("Xin hãy đợi 30 giây để gửi lại.");
        }

        final OtpDetail otpDetail = OtpUtils
            .findOtpByKey(otpKey)
            .orElseGet(() -> createOtp(otpKey));

        if (request.isUsingPhone()) {
            smsService.sendTo("+84 " + request.toPhone(), "OTP xác nhận của bạn là: {}", otpDetail.getValue());
        } else {
            this.mailWorker.sendMail(
                mail -> mail
                    .sendTo(request.getEmailOrPhone())
                    .withTemplate(EmailTemplate.Key.RESET_PASSWORD)
                    .withProperty("username", user.getUsername())
                    .withProperty("OTP", otpDetail.getValue())
                    .withSuccessMessage("Sent reset password message with OTP to {}", request.emailOrPhone)
            );
        }

        final OtpDetail savedOtp = this.otpRepository.save(otpDetail);
        log.info("Save otp succeed with id {} and value {}", savedOtp.getId(), savedOtp.getValue());

        RedisRepository.set(otpKey, savedOtp, Duration.ofMinutes(15));
        RedisRepository.set(resendKey, "", Duration.ofSeconds(30));

        if (request.isUsingPhone()) {
            return ApiResponse.success("Chúng tôi đã gửi mã OTP đến số điện thoại của bạn. Vui lòng sử dụng OTP đó để đặt lại mật khẩu của bạn.", otpKey);
        }
        return ApiResponse.success("Chúng tôi đã gửi OTP đến email của bạn. Vui lòng sử dụng OTP đó để đặt lại mật khẩu của bạn.", otpKey);
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
    public static class SendOTPResetPasswordRequest extends BaseRequest {
        @NotBlank(message = "Xin hãy nhập email hoặc số điện thoại")
        private String emailOrPhone;

        @NotNull
        private String toPhone() {
            if (StringEx.lengthOf(emailOrPhone) == 10) {
                return this.emailOrPhone.substring(1);
            }
            return this.emailOrPhone;
        }

        public boolean isUsingPhone() {
            return StringEx.hasDigitOnly(this.emailOrPhone);
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<SendOTPResetPasswordRequest> {

        private final UserRepository userRepository;

        @Override
        protected void validate() {
            this.rejectIfEmpty(SendOTPResetPasswordRequest.Fields.emailOrPhone, this::validateEmailOrPhone);
        }

        private String validateEmailOrPhone() {
            if (this.request.isUsingPhone()) {
                return this.validatePhone();
            }
            return this.validateEmail();
        }

        private String validateEmail() {
            final String email = request.emailOrPhone;
            if (!email.matches("^(.+)@(\\S+)$")) {
                throw ApiException.badRequest("Email không hợp lệ");
            }
            final User user = this.userRepository.findByEmail(email).orElseThrow(() -> ApiException.badRequest("Không tìm thấy email."));
            if (user.is(BANNED)) {
                return "Người dùng đã bị khóa";
            }
            DataContext.store(user);
            final Integer totalTried = ResetPasswordOTPVerificationCommand.otpRetryMapToUserId.getOrDefault(user.getId(), 0);
            if (totalTried >= ResetPasswordOTPVerificationCommand.MAX_TRY) {
                throw ApiException.badRequest("Tài khoản của bạn đã vượt quá số lần xác thực trong ngày. Xin hãy quay lại vào ngày mai.");
            }
            return null;
        }

        @Nullable
        private String validatePhone() {
            final String phone = request.toPhone();
            if (!phone.matches("^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$")) {
                return "Số điện thoại không hợp lệ.";
            }
            final User user = this.userRepository
                .findByPhoneEndingWith("%" + phone)
                .orElseThrow(() -> ApiException.badRequest("Không tìm thấy số điện thoại"));

            if (user.is(BANNED)) {
                return "Người dùng đã bị khóa";
            }
            DataContext.store(user);
            final Integer totalTried = ResetPasswordOTPVerificationCommand.otpRetryMapToUserId.getOrDefault(user.getId(), 0);
            if (totalTried >= ResetPasswordOTPVerificationCommand.MAX_TRY) {
                throw ApiException.badRequest("Tài khoản của bạn hiện không thể khôi phục mật khẩu.");
            }
            return null;
        }

    }
}
