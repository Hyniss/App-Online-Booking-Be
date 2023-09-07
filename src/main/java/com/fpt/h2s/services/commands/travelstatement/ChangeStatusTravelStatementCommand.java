package com.fpt.h2s.services.commands.travelstatement;

import ananta.utility.StringEx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.TravelStatement;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.TravelStatementRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.TravelStatementResponse;
import com.fpt.h2s.workers.MailWorker;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChangeStatusTravelStatementCommand implements BaseCommand<ChangeStatusTravelStatementCommand.ChangeStatusTravelStatementRequest, TravelStatementResponse> {
    private final TravelStatementRepository travelStatementRepository;
    private final UserRepository userRepository;
    private final MailWorker mailWorker;
    private final NotificationService notificationService;

    private static final Map<TravelStatement.Status, Set<TravelStatement.Status>> nextStatusesMapToCurrentStatusForBusinessAdmin = Map.ofEntries(
            Map.entry(TravelStatement.Status.PENDING, Set.of(TravelStatement.Status.REJECTED, TravelStatement.Status.APPROVED))
    );

    private static final Map<TravelStatement.Status, Set<TravelStatement.Status>> nextStatusesMapToCurrentStatusForBusinessMember = Map.ofEntries(
            Map.entry(TravelStatement.Status.PENDING, Set.of(TravelStatement.Status.CANCELED))
    );

    @Override
    public ApiResponse<TravelStatementResponse> execute(ChangeStatusTravelStatementCommand.ChangeStatusTravelStatementRequest request) {
        final TravelStatement travelStatement = this.travelStatementRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy tờ trình với id = {}.", request.getId()));

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        final User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));

        final TravelStatement.Status currentStatus = travelStatement.getStatus();
        final TravelStatement.Status nextStatus = request.getStatus();

        //Change status travel statement for business admin
        if(currentUser.is(User.Role.BUSINESS_ADMIN)) {
            if(!Objects.equals(travelStatement.getUser().getCompanyId(), currentUser.getCompanyId())) {
                throw ApiException.badRequest("Không thể thay đổi trạng thái của tờ trình này.");
            }

            if (!ChangeStatusTravelStatementCommand.nextStatusesMapToCurrentStatusForBusinessAdmin.containsKey(currentStatus)) {
                throw ApiException.badRequest("Không thể thay đổi trạng thái của tờ trình này.");
            }

            final Set<TravelStatement.Status> availableStatusesToChange = ChangeStatusTravelStatementCommand.nextStatusesMapToCurrentStatusForBusinessAdmin.get(currentStatus);
            if (!availableStatusesToChange.contains(nextStatus)) {
                throw ApiException.badRequest("Trạng thái cần thay đổi phải là một trong những trạng thái sau đây: {}.", availableStatusesToChange);
            }
        }

        //Change status travel statement for business member
        else if(currentUser.is(User.Role.BUSINESS_MEMBER)) {
            if(!Objects.equals(travelStatement.getUser().getId(), currentUserId)) {
                throw ApiException.badRequest("Không thể thay đổi trạng thái của tờ trình này.");
            }

            if (!ChangeStatusTravelStatementCommand.nextStatusesMapToCurrentStatusForBusinessMember.containsKey(currentStatus)) {
                throw ApiException.badRequest("Không thể thay đổi trạng thái của tờ trình này.");
            }

            final Set<TravelStatement.Status> availableStatusesToChange = ChangeStatusTravelStatementCommand.nextStatusesMapToCurrentStatusForBusinessMember.get(currentStatus);
            if (!availableStatusesToChange.contains(nextStatus)) {
                throw ApiException.badRequest("Trạng thái cần thay đổi phải là một trong những trạng thái sau đây: {}.", availableStatusesToChange);
            }
        }

        TravelStatement travelStatementToChange = travelStatement.toBuilder().status(request.getStatus()).rejectMessage(request.getRejectMessage()).build();
        if(nextStatus == TravelStatement.Status.APPROVED)
            travelStatementToChange = travelStatement.toBuilder().status(request.getStatus()).approvedAt(new Timestamp(System.currentTimeMillis())).rejectMessage(request.getRejectMessage()).build();
        this.travelStatementRepository.save(travelStatementToChange);

        if(currentUser.is(User.Role.BUSINESS_ADMIN)) {
            sendMail(travelStatement);
            if(nextStatus == TravelStatement.Status.APPROVED) {
                notificationService.send(notification -> notification.toUsers(travelStatement.getCreatorId()).withContent("Đơn của bạn " + travelStatement.getName() + " đã được chấp thuận bởi quản trị viên công ty."));
            }
            if(nextStatus == TravelStatement.Status.REJECTED) {
                notificationService.send(notification -> notification.toUsers(travelStatement.getCreatorId()).withContent("Đơn của bạn " + travelStatement.getName() + " đã bị từ chối bởi quản trị viên công ty."));
            }
        }

        return ApiResponse.success("Thay đổi trạng thái tờ trình thành công", TravelStatementResponse.of(travelStatementToChange));
    }

    public void sendMail(TravelStatement travelStatement) {
        this.mailWorker.sendMail(
                mail -> mail
                        .sendTo(travelStatement.getUser().getEmail())
                        .withTemplate(EmailTemplate.Key.CHANGE_TRAVEL_STATEMENT_STATUS)
                        .withProperty("username", travelStatement.getUser().getUsername())
                        .withProperty("name", travelStatement.getName())
                        .withProperty("status", travelStatement.getStatus())
                        .withSuccessMessage("Send mail successfully")
        );
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class ChangeStatusTravelStatementRequest extends BaseRequest {
        @NonNull
        private final Integer id;
        @NonNull
        private final TravelStatement.Status status;

        @Length(max = 512, message = "Xin vui lòng nhập lý do huỷ bỏ tờ trình trong khoảng 512 ký tự.")
        private final String rejectMessage;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<ChangeStatusTravelStatementCommand.ChangeStatusTravelStatementRequest> {
            private final TravelStatementRepository travelStatementRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(ChangeStatusTravelStatementCommand.ChangeStatusTravelStatementRequest.Fields.id, this::validateTravelStatementId);
                this.rejectIfEmpty(ChangeStatusTravelStatementCommand.ChangeStatusTravelStatementRequest.Fields.rejectMessage, this::validateMessage);
            }

            private String validateTravelStatementId() {

                this.travelStatementRepository
                        .findById(this.request.id)
                        .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy tờ trình với id = {}.", this.request.id));

                return null;
            }

            private String validateMessage() {
                if (this.request.status == TravelStatement.Status.REJECTED && StringEx.isBlank(this.request.rejectMessage)) {
                    return "Xin vui lòng điền lý do huỷ bỏ tờ trình";
                }
                return null;
            }

        }
    }
}
