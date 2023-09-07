package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Log4j2
@Service
@RequiredArgsConstructor
public class UpdateUserPasswordCommand implements BaseCommand<UpdateUserPasswordCommand.Request, Void> {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<Void> execute(Request request) {
        User userToUpdate = userRepository
            .getById(User.getCurrentId())
            .withPassword(passwordEncoder.encode(request.newPassword));

        userRepository.save(userToUpdate);

        log.info("Updated password for user with id {}", userToUpdate.getId());
        return ApiResponse.success("Thay đổi mật khẩu thành công");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        private String oldPassword;

        @NotBlank(message = "{PASSWORD_BLANK}")
        private String newPassword;

        @NotBlank(message = "{CONFIRM_PASSWORD_BLANK}")
        private String confirmPassword;

    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<Request> {

        private final Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).*$");

        private final UserRepository userRepository;

        private final PasswordEncoder passwordEncoder;

        @Override
        protected void validate() {
            this.rejectIfEmpty(Request.Fields.oldPassword, this::validateOldPassword);
            this.rejectIfEmpty(Request.Fields.newPassword, this::validateNewPassword);
            this.rejectIfEmpty(Request.Fields.confirmPassword, this::validateConfirmPassword);
        }

        private String validateOldPassword() {
            User user = userRepository.getById(User.currentUserId().orElseThrow(), "User not found.");
            String oldPassword = Optional.ofNullable(request.oldPassword).orElse("");

            if (user.getPassword() == null) {
                if (oldPassword.isBlank()) {
                    return null;
                }
                return "Wrong password";
            }

            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                return null;
            }

            return "Wrong password";
        }

        private String validateNewPassword() {
            final String password = this.request.newPassword;
            if (password.length() < 8 || password.length() > 32 || !this.pattern.matcher(password).matches()) {
                return "Password must contain a number, an uppercase letter, a lowercase letter and have total 8-32 characters";
            }
            return null;
        }

        private String validateConfirmPassword() {
            if (!Objects.equals(this.request.newPassword, this.request.confirmPassword)) {
                return "Confirm password have to match with password.";
            }
            return null;
        }

    }



}
