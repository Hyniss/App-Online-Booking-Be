package com.fpt.h2s.services.commands.company;

import ananta.utility.StringEx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.utilities.MoreStrings;
import com.fpt.h2s.workers.MailWorker;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fpt.h2s.models.entities.User.Role.BUSINESS_ADMIN;
import static com.fpt.h2s.models.entities.User.Role.BUSINESS_MEMBER;


@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class BusinessAdminAddBusinessUserCommand implements BaseCommand<BusinessAdminAddBusinessUserCommand.Request, Void> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CompanyRepository companyRepository;
    private final MailWorker mailWorker;

    @Override
    public ApiResponse<Void> execute(Request request) {
        final Company company = this.companyRepository.findById(getCompanyId()).orElseThrow();
        if (company.getStatus() != Company.Status.ACTIVE) {
            return ApiResponse.badRequest("Công ty của bạn chưa được kích hoạt để thêm thành viên.");
        }

        final User user = request.toBusinessMember(company.getId());
        User savedUser = this.userRepository.save(user);

        this.userProfileRepository.save(UserProfile.builder().userId(savedUser.getId()).build());
        this.sendVerificationTo(savedUser);
        return ApiResponse.success("Thêm thành viên thành công.");
    }

    private void sendVerificationTo(@NonNull final User savedUser) {
        this.mailWorker.sendMail(m -> m
                .sendTo(savedUser.getEmail())
                .withTemplate(EmailTemplate.Key.BUSINESS_EMAIL_VERIFICATION)
                .withProperty("username", savedUser.getUsername())
                .withSuccessMessage("Sent verification email to user with id {}", savedUser.getId()));
    }

    private Integer getCompanyId() {
        Integer adminId = User.currentUserId()
                .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));
        final User businessAdmin = this.userRepository.findById(adminId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", adminId));
        return businessAdmin.getCompanyId();
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NotBlank(message = "Xin vui lòng nhập email.")
        @Email(message = "Xin vui lòng nhập email hợp lệ.")
        private String email;

        @NotBlank(message = "Xin vui lòng chọn mã vùng điện thoại.")
        @Pattern(regexp = "^(\\+?\\d{1,3}|\\d{1,4})$", message = "Xin vui lòng nhập mã vùng điện thoại hợp lệ.")
        private final String region;

        @NotBlank(message = "Xin vui lòng nhập số điện thoại.")
        @Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$", message = "Xin vui lòng nhập số điện thoại hợp lệ.")
        private String phone;

        public String toPhone() {
            final String phone = this.phone.length() == 10 ? this.phone.substring(1) : this.phone;
            return StringEx.format("+{} {}", this.region, phone);
        }

        @NotBlank(message = "Xin vui lòng nhập tên người dùng.")
        @Length(min = 4, max = 32, message = "Tên người dùng phải có độ dài từ 4 đến 32 ký tự.")
        @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "Tên người dùng chỉ chứa a-z, A-Z, khoảng trắng.")
        private String username;

        private final User.Role role;

        public User toBusinessMember(Integer companyId) {
            return User.builder()
                    .email(this.email)
                    .phone(this.toPhone())
                    .username(this.username)
                    .roles(this.role.name())
                    .status(User.Status.PENDING)
                    .companyId(companyId)
                    .build();
        }

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<Request> {

            private final UserRepository userRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.email, this::validateEmail);
                this.rejectIfEmpty(Fields.phone, this::validatePhone);
                this.rejectIfEmpty(Fields.role, this::validateRoles);
            }

            private String validateEmail() {
                if (this.userRepository.existsByEmail(this.request.email)) {
                    return "Xin vui lòng nhập email khác. Email này đã được sử dụng";
                }
                return null;
            }

            private String validatePhone() {
                if (this.userRepository.existsByPhone(this.request.toPhone())) {
                    return "Xin vui lòng nhập số điện thoại khác. Số điện thoại này đã được sử dụng";
                }
                return null;
            }

            private String validateRoles() {
                final Set<User.Role> allowedRoles = Set.of(BUSINESS_ADMIN, BUSINESS_MEMBER);
                if (!allowedRoles.contains(this.request.role)) {
                    return MoreStrings.format("Quyền của thành viên phải là một trong các quyền sau đây: {}.", allowedRoles);
                }
                return null;
            }
        }

    }
}
