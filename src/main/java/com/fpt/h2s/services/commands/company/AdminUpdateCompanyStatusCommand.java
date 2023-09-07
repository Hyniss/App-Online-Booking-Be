package com.fpt.h2s.services.commands.company;

import ananta.utility.StringEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.CompanyResponse;
import com.fpt.h2s.workers.MailWorker;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static com.fpt.h2s.models.entities.Company.Status.*;
import static com.fpt.h2s.models.entities.EmailTemplate.Key.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminUpdateCompanyStatusCommand implements BaseCommand<AdminUpdateCompanyStatusCommand.Request, CompanyResponse> {

    private static final int MAX_TOTAL_REJECT = 3;

    private static final Map<Pair<Company.Status, Company.Status>, StatusMessage> NOTIFICATIONS_AND_EMAIL_MAP_TO_STATUSES = Map.ofEntries(
        StatusMessage.fromStatus(ACTIVE).toStatus(INACTIVE).withEmailTemplate(ADMIN_BAN_COMPANY_REQUEST).andNotificationContent("Công ty của bạn đã bị khóa bởi quản trị viên. Xin hãy liên hệ với chúng tôi nếu đó là sai sót bên chúng tôi.").toEntry(),
        StatusMessage.fromStatus(INACTIVE).toStatus(ACTIVE).withEmailTemplate(ADMIN_UNBAN_COMPANY_REQUEST).andNotificationContent("Chúng tôi đã mở lại công ty của bạn. Xin lỗi vì sự cố không đáng có này.").toEntry(),
        StatusMessage.fromStatus(PENDING).toStatus(ACTIVE).withEmailTemplate(ADMIN_ACCEPT_COMPANY_REQUEST).andNotificationContent("Chúc mừng bạn, đơn yêu cầu hợp tác công ty của bạn đã được chấp thuận.").toEntry(),
        StatusMessage.fromStatus(PENDING).toStatus(INACTIVE).withEmailTemplate(ADMIN_SUSPEND_COMPANY_REQUEST).andNotificationContent("Công ty của bạn đã bị khóa bởi quản trị viên do chúng tôi nghi ngờ công ty bạn có hành vi không phù hợp với hệ thống của chúng tôi.").toEntry(),
        StatusMessage.fromStatus(PENDING).toStatus(REJECTED).withEmailTemplate(ADMIN_SUSPEND_COMPANY_REQUEST).andNotificationContent("Đơn yêu cầu hợp tác công ty của bạn đã bị từ chối do chúng tôi không thể xác minh công ty.").toEntry(),
        StatusMessage.fromStatus(PENDING).toStatus(PENDING_CHANGE).withEmailTemplate(ADMIN_REJECT_COMPANY_REQUEST).andNotificationContent("Đơn yêu cầu hợp tác công ty của bạn đã bị từ chối do chúng tôi không thể xác minh công ty. Xin hãy gửi lại yêu cầu mới có thông tin hợp lệ về công ty của bạn.").toEntry()
    );

    private static final Map<Company.Status, Set<Company.Status>> nextStatusesMapToCurrentStatus = Map.ofEntries(
        Map.entry(PENDING, Set.of(ACTIVE, REJECTED, INACTIVE)),
        Map.entry(ACTIVE, Set.of(INACTIVE)),
        Map.entry(INACTIVE, Set.of(ACTIVE))
    );

    private final MailWorker mailWorker;

    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<CompanyResponse> execute(final Request request) {
        final Integer companyId = request.getCompanyId();
        final Company company = this.companyRepository.getById(companyId, "Không tìm thấy công ty.");

        final Company.Status currentStatus = company.getStatus();
        final Company.Status nextStatus = request.status;

        if (!AdminUpdateCompanyStatusCommand.nextStatusesMapToCurrentStatus.containsKey(currentStatus)) {
            throw ApiException.badRequest("Không thể thay đổi trạng thái của công ty.");
        }
        final Set<Company.Status> availableStatusesToChange = AdminUpdateCompanyStatusCommand.nextStatusesMapToCurrentStatus.get(currentStatus);
        if (!availableStatusesToChange.contains(nextStatus)) {
            throw ApiException.badRequest("Trạng thái mới phải là một trong những trạng thái sau đây {}", availableStatusesToChange);
        }

        final Company updatedCompany = this.updateStatus(company, request);

        this.sendMailAndNotificationToOwnerOf(updatedCompany, currentStatus);

        CompanyResponse response = CompanyResponse.of(updatedCompany);
        if (updatedCompany.getStatus() == REJECTED || updatedCompany.getStatus() == PENDING_CHANGE) {
            return ApiResponse.success("Từ chối đơn yêu cầu thành công", response);
        }
        if (updatedCompany.getStatus() == ACTIVE) {
            return ApiResponse.success("Chấp thuận đơn yêu cầu thành công", response);
        }

        return ApiResponse.success("Thay đổi trạng thái công ty thành công.", response);
    }

    private void sendMailAndNotificationToOwnerOf(final Company company, final Company.Status oldStatus) {
        final Integer ownerId = company.getOwnerId();
        final User owner = this.userRepository.getById(ownerId, "Không thể tìm thấy người dùng");

        final Company.Status newStatus = company.getStatus();
        final Pair<Company.Status, Company.Status> statusPair = Pair.of(oldStatus, newStatus);
        if (!NOTIFICATIONS_AND_EMAIL_MAP_TO_STATUSES.containsKey(statusPair)) {
            return;
        }
        final StatusMessage statusMessage = NOTIFICATIONS_AND_EMAIL_MAP_TO_STATUSES.get(statusPair);

        this.mailWorker.sendMail(
            mail -> mail
                .sendTo(owner.getEmail())
                .withTemplate(statusMessage.emailTemplate)
                .withProperty("username", owner.getUsername())
                .withSuccessMessage("Sent status notification mail to owner of company {}", company.getName())
        );
        this.notificationService
            .send(notification -> notification
                .toUser(company.getOwnerId())
                .withContent(statusMessage.notificationMessage)
            );
    }

    private Company updateStatus(final Company company, final Request request) {
        final Company.Status nextStatus = request.status;
        if (nextStatus != REJECTED) {
            final Company companyToUpdate = company.withStatus(nextStatus);
            return this.companyRepository.save(companyToUpdate);
        }
        final int newTotalReject = company.getTotalRejected() + 1;
        final boolean canUpdateInformationToResendRequest = newTotalReject < AdminUpdateCompanyStatusCommand.MAX_TOTAL_REJECT;

        final Company.Status nextRejectStatus = canUpdateInformationToResendRequest ? PENDING_CHANGE : REJECTED;

        final Company companyToUpdate = company
            .withStatus(nextRejectStatus)
            .withRejectMessage(request.rejectMessage)
            .withTotalRejected(newTotalReject);
        return this.companyRepository.save(companyToUpdate);
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        @NotNull
        private final Integer companyId;
        @NotNull
        private final Company.Status status;

        @Length(max = 512, message = "Lý do từ chối phải nằm trong khoảng 512 ký tự.")
        private final String rejectMessage;

        @Component
        @RequiredArgsConstructor
        static class Validator extends BaseValidator<Request> {

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.rejectMessage, this::validateMessage);
            }

            private String validateMessage() {
                if (this.request.status == REJECTED && StringEx.isBlank(this.request.rejectMessage)) {
                    return "Vui lòng nhập lý do từ chối.";
                }
                return null;
            }

        }
    }

    @Getter
    private static class StatusMessage {

        private Company.Status fromStatus;
        private Company.Status toStatus;
        private EmailTemplate.Key emailTemplate;
        private String notificationMessage;

        public static StatusMessage fromStatus(final Company.Status status) {
            final StatusMessage statusMessage = new StatusMessage();
            statusMessage.fromStatus = status;
            return statusMessage;
        }

        public StatusMessage toStatus(final Company.Status status) {
            this.toStatus = status;
            return this;
        }

        public StatusMessage withEmailTemplate(final EmailTemplate.Key emailTemplate) {
            this.emailTemplate = emailTemplate;
            return this;
        }

        public StatusMessage andNotificationContent(final String notificationMessage) {
            this.notificationMessage = notificationMessage;
            return this;
        }
        public Map.Entry<Pair<Company.Status, Company.Status>, StatusMessage> toEntry() {
            return Map.entry(Pair.of(this.fromStatus, this.toStatus), this);
        }
    }
}
