package com.fpt.h2s.services.commands.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class ResetPasswordCommand implements BaseCommand<ResetPasswordCommand.ResetPasswordRequest, Void> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public ApiResponse<Void> execute(final ResetPasswordRequest request) {
        int userId = Integer.parseInt(RedisRepository.get(request.verificationToken).orElseThrow(() -> ApiException.forbidden("Phiên khôi phục đã hết hạn, xin hãy thử lại.")));

        final User user = this.userRepository.findById(userId).orElseThrow();
        final User userToUpdate = user
            .withPassword(this.passwordEncoder.encode(request.password))
            .withStatus(User.Status.ACTIVE);
        this.userRepository.save(userToUpdate);

        RedisRepository.remove(request.getVerificationToken());
        ResetPasswordCommand.log.info("Reset password succeed.");
        return ApiResponse.success("Khôi phục mật khẩu thành công");
    }
    
    @Getter
    @NoArgsConstructor
    @Setter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class ResetPasswordRequest extends BaseRequest {

        @NotBlank(message = "Please fill verification token")
        private String verificationToken;

        @Length(min = 8, max = 32, message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự")
        @jakarta.validation.constraints.Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).*$", message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự")
        private String password;

        @NotBlank(message = "Xin hãy nhập mật khẩu xác nhận")
        private String confirmPassword;
    }
    
    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<ResetPasswordCommand.ResetPasswordRequest> {
        
        @Override
        protected void validate() {
            this.rejectIfEmpty(ResetPasswordRequest.Fields.confirmPassword, this::validateConfirmPassword);
        }

        private String validateConfirmPassword() {
            if (!Objects.equals(this.request.password, this.request.confirmPassword)) {
                return "Mật khẩu xác nhận phải khớp với mật khẩu";
            }
            return null;
        }
        
    }
    
}
