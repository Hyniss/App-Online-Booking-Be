package com.fpt.h2s.services.commands.company;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SendCooperateRequestToAdminCommand implements BaseCommand<SendCooperateRequestToAdminCommand.Request, Void> {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;

    @Override
    public ApiResponse<Void> execute(final Request request) {
        final Integer userId = User.currentUserId().orElseThrow();
        final User businessUser = this.userRepository.findById(userId).orElseThrow();
        final Integer companyId = businessUser.getCompanyId();
        if (companyId == null) {
            throw ApiException.forbidden("Người dùng không thuộc công ty nào.");
        }
        final Company company = this.companyRepository.findById(companyId).orElseThrow();
        if (company.getStatus() != Company.Status.PENDING_CHANGE) {
            throw ApiException.badRequest("Không thể gửi yêu cầu hợp tác đến quản trị viên.");
        }

        final Company companyToUpdate = company.withStatus(Company.Status.PENDING);
        this.companyRepository.save(companyToUpdate);

        List<Integer> adminIds = userRepository.findAllUsersByRole(User.Role.ADMIN.name()).stream().map(User::getId).toList();
        notificationService.send(notification -> notification.toUsers(adminIds).withContent("Bạn có yêu cầu phê duyệt hợp tác công ty."));

        return ApiResponse.success("Đã gửi yêu cầu hợp tác. Vui lòng đợi cho đến khi chúng tôi liên hệ với bạn.");
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

    }
}
