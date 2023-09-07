package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.OtpDetail;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.OtpRepository;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.OtpUtils;
import com.fpt.h2s.utilities.Tokens;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class OTPVerificationCommand implements BaseCommand<OTPVerificationCommand.Request, String> {
    public static final String SECRET_KEY = "12376yjsdbcbs8ag9dhs9f9qhi4bwai91279r8dahfsg89qwdbhuabsusn";
    private final OtpRepository otpRepository;
    public static final int MAX_TRY = 5;

    @Override
    public ApiResponse<String> execute(final Request request) {
        final OtpDetail otp = OtpUtils
            .findOtpByKey(request.key)
            .orElseThrow(() -> ApiException.badRequest("Không tìm thấy mã OTP. Xin hãy gửi lại để lấy mã khác."));

        try {
            OtpUtils.validateOtp(otp, request.otp, MAX_TRY);
        } catch (final OtpUtils.WrongOtpException e) {
            throw ApiException.badRequest("Mã OTP không đúng. Bạn còn {} lần nhập.", e.getTryLeft());
        } catch (final OtpUtils.OtpDisabledException e) {
            final String disableKey = StringEx.format("{}-Disabled", request.key);
            RedisRepository.set(disableKey, "", Duration.ofDays(1));
            throw ApiException.badRequest("Số điện thoại đã vượt quá số lần xác thực trong ngày. Xin hãy quay lại vào ngày mai.");
        }

        final OtpDetail otpToSave = otp.markVerified();
        this.otpRepository.save(otpToSave);

        final String token = Tokens.generateToken(
            Map.ofEntries(
                Map.entry("key", request.key),
                Map.entry("ip", MoreRequests.getIPAddress()),
                Map.entry("otp", otp.getValue()),
                Map.entry("time", LocalDateTime.now().toString()),
                Map.entry("phone", request.id != null ? request.id : request.toPhone())
            ),
            Duration.ofMinutes(30), SECRET_KEY
        );

        RedisRepository.remove(request.key);
        RedisRepository.set(token, request.id != null ? request.id : request.toPhone(), Duration.ofMinutes(15));

        return ApiResponse.success("Xác thực thành công", token);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotBlank
        private final String key;

        private final String id;

        private final String region;

        private final String phone;
        @NotBlank(message = "Xin hãy nhập OTP")
        private final String otp;

        public String toPhone() {
            if (region == null || phone == null) {
                return null;
            }
            if (!region.matches("^(\\+?\\d{1,3}|\\d{1,4})$")) {
                throw ApiException.badRequest("Quốc gia không hợp lệ.");
            }

            if (!phone.matches("^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$")) {
                throw ApiException.badRequest("Số điện thoại không hợp lệ.");
            }

            final String phone = this.phone.length() == 10 ? this.phone.substring(1) : this.phone;
            return StringEx.format("+{} {}", this.region, phone);
        }
    }
}