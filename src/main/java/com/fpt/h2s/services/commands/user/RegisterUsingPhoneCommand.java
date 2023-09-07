package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.MoreStrings;
import jakarta.transaction.Transactional;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.fpt.h2s.models.entities.User.Role.CUSTOMER;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUsingPhoneCommand implements BaseCommand<RegisterUsingPhoneCommand.Request, Void> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<Void> execute(final Request request) {
        final String redisToken = request.getToken();
        final String phoneNumber = RedisRepository
            .get(redisToken)
            .orElseThrow(() -> ApiException.badRequest("Token is invalid"));

        final User user = User.builder()
            .phone(phoneNumber)
            .username(request.getUsername())
            .password(this.passwordEncoder.encode(request.getPassword()))
            .roles(String.join(", ", CUSTOMER.name()))
            .status(User.Status.ACTIVE)
            .build();

        final User savedUser = this.userRepository.save(user);

        final UserProfile profile = UserProfile.builder().userId(savedUser.getId()).build();
        this.userProfileRepository.save(profile);
        RedisRepository.remove(redisToken);

        log.info("Created new user using phone {}", phoneNumber);
        return ApiResponse.success("Đăng ký thành công.");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    @GroupSequence({Request.class, BaseValidator.FirstOrder.class, BaseValidator.SecondOrder.class, BaseValidator.ThirdOrder.class})
    public static class Request extends BaseRequest {

        @NotBlank(message = "Invalid token", groups = BaseValidator.FirstOrder.class)
        private final String token;

        @NotBlank(message = "Xin hãy nhập tên người dùng", groups = BaseValidator.FirstOrder.class)
        @Length(min = 4, max = 32, message = "Tên người dùng chỉ chứa a-z, A-Z, khoảng trắng và có độ dài từ 4 đến 32 kí tự", groups = BaseValidator.SecondOrder.class)
        private final String username;

        @NotBlank(message = "Xin hãy nhập mật khẩu.", groups = BaseValidator.FirstOrder.class)
        @Length(min = 8, max = 32, message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự", groups = BaseValidator.SecondOrder.class)
        @jakarta.validation.constraints.Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).*$", message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự", groups = BaseValidator.ThirdOrder.class)
        private final String password;

        @NotBlank(message = "Xin hãy nhập mật khẩu xác nhận", groups = BaseValidator.FirstOrder.class)
        private final String confirmPassword;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.username, this::validateUsername);
                this.rejectIfEmpty(Fields.confirmPassword, this::validateConfirmPassword);
            }


            private String validateConfirmPassword() {
                if (!Objects.equals(this.request.password, this.request.confirmPassword)) {
                    return "Mật khẩu xác nhận phải khớp với mật khẩu";
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
