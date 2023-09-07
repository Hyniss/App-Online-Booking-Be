package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.user.utils.AuthUtils;
import com.fpt.h2s.services.commands.user.utils.Urls;
import com.fpt.h2s.utilities.MoreStrings;
import com.fpt.h2s.workers.MailWorker;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

import static com.fpt.h2s.models.entities.User.Role.CUSTOMER;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUsingEmailCommand implements BaseCommand<RegisterUsingEmailCommand.Request, Void> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailWorker mailWorker;
    private final ConsulConfiguration consul;

    @Override
    public ApiResponse<Void> execute(final Request request) {
        final User user = User.builder()
            .email(request.getEmail())
            .password(this.passwordEncoder.encode(request.getPassword()))
            .username(request.getUsername())
            .roles(String.join(", ", CUSTOMER.name()))
            .status(User.Status.PENDING)
            .build();

        final User savedUser = this.userRepository.save(user);

        final UserProfile profile = UserProfile.builder().userId(savedUser.getId()).build();
        this.userProfileRepository.save(profile);

        this.sendVerificationTo(savedUser);
        RegisterUsingEmailCommand.log.info("Created new user using email {}", request.getEmail());
        return ApiResponse.success("Bạn đã đăng ký thành công. Xin hãy kiểm tra email của bạn để xác thực tài khoản");
    }

    private void sendVerificationTo(@NonNull final User savedUser) {
        final Duration tokenDuration = Duration.ofDays(3);
        final String secretKey = this.consul.get("secret-key.VERIFY_EMAIL_ADDRESS");
        final String redisKey = "user-id-%s-verification-token".formatted(savedUser.getId());
        final String token = AuthUtils.generateVerifyCode(savedUser, redisKey, tokenDuration, secretKey);

        this.mailWorker.sendMail(
            mail -> mail
                .sendTo(savedUser.getEmail())
                .withTemplate(EmailTemplate.Key.EMAIL_VERIFICATION)
                .withProperty("username", savedUser.getUsername())
                .withProperty("url", Urls.verifyEmailUrlOf(token))
                .withSuccessMessage("Sent verification email to user with id {}", savedUser.getId())
        );
        RedisRepository.set(redisKey, token, tokenDuration);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
//    @GroupSequence({Request.class, BaseValidator.FirstOrder.class, BaseValidator.SecondOrder.class, BaseValidator.ThirdOrder.class})
    public static class Request extends BaseRequest {

        @NotBlank
            (message = "Xin hãy nhập email"
//                groups = BaseValidator.FirstOrder.class
            )
        @Email(
            message = "Email không hợp lệ"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String email;

        @NotBlank(
            message = "Xin hãy nhập tên người dùng"
//            groups = BaseValidator.FirstOrder.class
        )
        @Length(
            min = 4, max = 32, message = "Tên người dùng chỉ chứa a-z, A-Z, khoảng trắng và có độ dài từ 4 đến 32 kí tự"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String username;

        @NotBlank(
            message = "Xin hãy nhập mật khẩu."
//            groups = BaseValidator.FirstOrder.class
        )
        @Length(
            min = 8, max = 32, message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự"
//            groups = BaseValidator.SecondOrder.class
        )
        @Pattern(
            regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).*$", message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự"
//            groups = BaseValidator.ThirdOrder.class
        )
        private final String password;

        @NotBlank(
            message = "Xin hãy nhập mật khẩu xác nhận"
//            groups = BaseValidator.FirstOrder.class
        )
        private final String confirmPassword;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final UserRepository userRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.email, this::validateEmail);
                this.rejectIfEmpty(Fields.username, this::validateUsername);
                this.rejectIfEmpty(Fields.confirmPassword, this::validateConfirmPassword);
            }

            private String validateConfirmPassword() {
                if (!Objects.equals(this.request.password, this.request.confirmPassword)) {
                    return "Mật khẩu xác nhận phải khớp với mật khẩu";
                }
                return null;
            }

            private String validateEmail() {
                if (this.userRepository.existsByEmail(this.request.email)) {
                    return "Email đã được sử dụng, xin hãy sử dụng email khác";
                }
                return null;
            }

            private String validateUsername() {
                String unaccentUsername = MoreStrings.unaccent(request.username);
                if (!unaccentUsername.matches("^[A-Za-z\\s]+$")) {
                    return "Tên người dùng chỉ chứa a-z, A-Z, khoảng trắng và có độ dài từ 4 đến 32 kí tự";
                }
                return null;
            }
        }
    }
}
