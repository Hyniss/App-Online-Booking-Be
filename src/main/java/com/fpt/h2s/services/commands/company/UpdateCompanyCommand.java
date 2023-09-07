package com.fpt.h2s.services.commands.company;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.user.RegisterForBusinessOwnerCommand;
import com.fpt.h2s.utilities.MoreStrings;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateCompanyCommand implements BaseCommand<UpdateCompanyCommand.Request, Void> {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<Void> execute(final Request request) {
        final Integer ownerId = User.currentUserId().orElseThrow();
        final User owner = this.userRepository.findById(ownerId).orElseThrow();
        if (owner.getCompanyId() == null) {
            throw ApiException.badRequest("Người dùng không có công ty.");
        }
        final Company company = owner.getCompany();
        if (!Objects.equals(request.companyId, owner.getCompanyId())) {
            throw ApiException.badRequest("Người dùng không thuộc về công ty này.");
        }
        if (company.getStatus() == Company.Status.REJECTED || company.getStatus() == Company.Status.INACTIVE) {
            throw ApiException.badRequest("Công ty của bạn hiện không thế cập nhật được.");
        }

        final Company companyToUpdate = company.toBuilder()
            .name(request.companyName)
            .shortName(request.shortName)
            .size(request.size)
            .address(request.address)
            .quotaCode(request.taxCode)
            .contactName(request.contactName)
            .contact(request.contactNumber)
            .build();
        this.companyRepository.save(companyToUpdate);
        return ApiResponse.success("Cập nhật thành công.");
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotNull
        private final Integer companyId;

        @NotBlank(message = "Xin hãy nhập tên công ty")
        @Length(min = 3, max = 255, message = "Tên công ty phải có độ dài từ 3-255 kí tự")
        private final String companyName;

        @NotBlank(message = "Xin hãy nhập tên viết tắt")
        @Length(max = 12, message = "Tên viết tắt phải có độ dài nhỏ hơn 12 kí tự.")
        private final String shortName;

        @NotNull(message = "Xin hãy chọn quy mô công ty")
        private final Company.Size size;
        @NotBlank(message = "Xin hãy nhập địa chỉ công ty")
        @Length(min = 5, max = 255, message = "Địa chỉ công ty phải có độ dài từ 5-255 kí tự")
        private final String address;

        @NotBlank(message = "Xin hãy nhập mã số thuế")
        @Pattern(regexp = "^[0-9]{10}$", message = "Mã số thuế không hợp lệ")
        private final String taxCode;

        @NotBlank(message = "Xin hãy nhập số điện thoại liên hệ")
        @Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$", message = "Số điện thoại không hợp lệ")
        private final String contactNumber;

        @NotBlank(message = "Xin hãy nhập tên liên hệ")
        @Length(min = 4, max = 32, message = "Tên liên hệ phải có độ dài từ 4 đến 32 kí tự")
        private final String contactName;
    }

    @Component
    @RequiredArgsConstructor
    public static class Validator extends BaseValidator<Request> {

        private final CompanyRepository companyRepository;

        @Override
        protected void validate() {
            this.rejectIfEmpty(RegisterForBusinessOwnerCommand.Request.Fields.contactName, this::validateContactName);
            this.rejectIfEmpty(RegisterForBusinessOwnerCommand.Request.Fields.companyName, this::validateCompanyName);
        }

        private String validateCompanyName() {
            Boolean isNameExisted = companyRepository
                .findOneByNameIgnoreCase(request.companyName)
                .map(Company::getId)
                .map(id -> !request.companyId.equals(id))
                .orElse(false);
            if (isNameExisted) {
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
