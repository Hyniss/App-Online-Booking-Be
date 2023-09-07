package com.fpt.h2s.services.commands.company;

import ananta.utility.StringEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CompanyResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fpt.h2s.models.entities.Company.Status.*;

@Service
@RequiredArgsConstructor
public class ViewDetailedCompanyForBusinessUserCommand implements BaseCommand<ViewDetailedCompanyForBusinessUserCommand.Request, CompanyResponse> {

    private static final List<Company.Status> NON_EDITABLE_STATUSES = List.of(REJECTED, INACTIVE);
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public ApiResponse<CompanyResponse> execute(final Request request) {
        final Integer userId = User.currentUserId().orElseThrow();
        final User businessUser = this.userRepository.findById(userId).orElseThrow();
        final Integer companyId = businessUser.getCompanyId();
        if (companyId == null) {
            throw ApiException.forbidden("Người dùng không thuộc công ty nào.");
        }
        final Company company = this.companyRepository.findById(companyId).orElseThrow();
        final CompanyResponse response = CompanyResponse
            .of(company)
            .withIsEditable(!NON_EDITABLE_STATUSES.contains(company.getStatus()) && userId.equals(company.getOwnerId()))
            .withRejectMessages(getRejectMessages(company))
            .withOwner(UserResponse.of(company.getOwner()));
        return ApiResponse.success(response);
    }
    @NotNull
    private static List<String> getRejectMessages(final Company company) {
        if (company.getStatus() == ACTIVE) {
            return Collections.emptyList();
        }
        if (company.getStatus() == INACTIVE) {
            return List.of("Công ty của bạn hiện đã bị khoá. Xin hãy liên hệ với quản trị viên nếu đây là sai sót của quản trị viên.");
        }
        if (StringEx.isBlank(company.getRejectMessage())) {
            return Collections.emptyList();
        }
        final ArrayList<String> messages = Lists.newArrayList("Quản trị viên đã từ chối yêu cầu hợp tác của bạn với lý do là: " + company.getRejectMessage());
        if (company.getStatus() == PENDING_CHANGE) {
            messages.add("Nếu bạn muốn hợp tác với chúng tôi, hãy thay đổi chính xác thông tin công ty của bạn và gửi lại đơn yêu cầu cho chúng tôi.");
        }
        return messages;
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

    }
}
