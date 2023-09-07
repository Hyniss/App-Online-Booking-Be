package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.user.utils.AuthUtils;
import com.fpt.h2s.services.commands.user.utils.Urls;
import com.fpt.h2s.utilities.MoreStrings;
import com.fpt.h2s.workers.MailWorker;
import jakarta.transaction.Transactional;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

import java.time.Duration;
import java.util.Objects;

import static com.fpt.h2s.models.entities.Company.Status.PENDING_CHANGE;
import static com.fpt.h2s.models.entities.User.Role.BUSINESS_OWNER;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterForBusinessOwnerCommand implements BaseCommand<RegisterForBusinessOwnerCommand.Request, Void> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailWorker mailWorker;
    private final ConsulConfiguration consul;

    @Override
    public ApiResponse<Void> execute(final Request request) {
        final User owner = saveUserFrom(request);

        final UserProfile profile = UserProfile.builder().userId(owner.getId()).build();
        this.userProfileRepository.save(profile);

        final Company company = this.createCompany(request, owner);
        this.userRepository.save(owner.withCompanyId(company.getId()));

        this.sendVerificationTo(owner);
        log.info("Registered new business account with email {}", request.getEmail());
        return ApiResponse.success("Bạn đã đăng ký thành công. Xin hãy kiểm tra email của bạn để xác thực tài khoản");
    }
    private User saveUserFrom(Request request) {
        final User user = User.builder()
            .email(request.getEmail())
            .password(this.passwordEncoder.encode(request.getPassword()))
            .username(request.getUsername())
            .roles(BUSINESS_OWNER.name())
            .status(User.Status.PENDING)
            .build();

        return this.userRepository.save(user);
    }
    private Company createCompany(final Request request, final User owner) {
        final Company company = request.toCompany(owner.getId()).withStatus(PENDING_CHANGE);
        return this.companyRepository.save(company);
    }

    private void sendVerificationTo(final User savedUser) {
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
    @GroupSequence({Request.class, BaseValidator.FirstOrder.class, BaseValidator.SecondOrder.class, BaseValidator.ThirdOrder.class})
    public static class Request extends BaseRequest {

        @NotBlank(message = "Xin hãy nhập email"
//            groups = BaseValidator.FirstOrder.class
        )
        @Email(message = "Email không hợp lệ"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String email;

        @NotBlank(message = "Xin hãy nhập tên người dùng"
//            groups = BaseValidator.FirstOrder.class
        )
        @Length(min = 4, max = 32, message = "Tên người dùng phải có độ dài từ 4 đến 32 kí tự"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String username;

        @NotBlank(message = "Xin hãy nhập mật khẩu."
//            groups = BaseValidator.FirstOrder.class
        )
        @Length(min = 8, max = 32, message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự"
//            groups = BaseValidator.SecondOrder.class
        )
        @Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).*$", message = "Mật khẩu phải chứa kí tự in hoa, kí tự viết thường, chữ số và có độ dài từ 8 đến 32 kí tự"
//            groups = BaseValidator.ThirdOrder.class
        )
        private final String password;

        @NotBlank(message = "Xin hãy nhập mật khẩu xác nhận"
//            groups = BaseValidator.FirstOrder.class
        )
        private final String confirmPassword;

        @NotNull(message = "Xin hãy chọn quy mô công ty"
//            groups = BaseValidator.FirstOrder.class
        )
        private final Company.Size size;

        @NotBlank(message = "Xin hãy nhập tên công ty"
//            groups = BaseValidator.FirstOrder.class
        )
        @Length(min = 3, max = 255, message = "Tên công ty phải có độ dài từ 3-255 kí tự"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String companyName;

        @NotBlank(message = "Xin hãy nhập địa chỉ công ty"
//            groups = BaseValidator.FirstOrder.class
        )
        @Length(min = 5, max = 255, message = "Địa chỉ công ty phải có độ dài từ 5-255 kí tự"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String companyAddress;

        @NotBlank(message = "Xin hãy nhập mã số thuế"
//            groups = BaseValidator.FirstOrder.class
        )
        @Pattern(regexp = "^[0-9]{10}$", message = "Mã số thuế không hợp lệ"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String taxCode;

        @NotBlank(message = "Xin hãy nhập tên liên hệ"
//            groups = BaseValidator.FirstOrder.class
        )
        @Length(min = 4, max = 32, message = "Tên liên hệ phải có độ dài từ 4 đến 32 kí tự"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String contactName;

        @NotBlank(message = "Xin hãy nhập số điện thoại liên hệ"
//            groups = BaseValidator.FirstOrder.class
        )
        @Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$", message = "Số điện thoại không hợp lệ"
//            groups = BaseValidator.SecondOrder.class
        )
        private final String contactNumber;

        private Company toCompany(final Integer companyOwnerId) {
            return Company.builder().contactName(this.contactName)
                .address(this.companyAddress)
                .discount(5)
                .quotaCode(this.taxCode)
                .size(this.size)
                .ownerId(companyOwnerId)
                .contact(this.contactNumber)
                .contactName(this.contactName)
                .name(this.companyName)
                .creatorId(companyOwnerId)
                .build();
        }

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final UserRepository userRepository;
            private final CompanyRepository companyRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.email, this::validateEmail);
                this.rejectIfEmpty(Fields.username, this::validateUsername);
                this.rejectIfEmpty(Fields.contactName, this::validateContactName);
                this.rejectIfEmpty(Fields.companyName, this::validateCompanyName);
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
                if (!MoreStrings.unaccent(request.username).matches("^[A-Za-z\\s]+$")) {
                    return "Tên người dùng chỉ chứa a-z, A-Z và khoảng trắng";
                }
                return null;
            }

            private String validateCompanyName() {
                if (companyRepository.existsByNameIgnoreCase(request.companyName.trim())) {
                    return "Tên công ty đã được sử dụng.";
                }
                return null;
            }

            private String validateContactName() {
                if (!MoreStrings.unaccent(request.contactName).matches("^[A-Za-z\\s]+$")) {
                    return "Tên liên hệ chỉ chứa a-z, A-Z và khoảng trắng";
                }
                return null;
            }
        }
    }
}
