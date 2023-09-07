package com.fpt.h2s.services.commands.contract;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.workers.MailWorker;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import static com.fpt.h2s.models.entities.Contract.Status.*;

@Service
@RequiredArgsConstructor
public class AdminChangeStatusContractCommand implements BaseCommand<AdminChangeStatusContractCommand.AdminChangeStatusContractRequest, Void> {
    private final ContractRepository contractRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final MailWorker mailWorker;
    private final NotificationService notificationService;


    private static final Map<Contract.Status, Set<Contract.Status>> nextStatusesMapToCurrentStatus = Map.ofEntries(
            Map.entry(APPROVED, Set.of(TERMINATED))
    );

    @Override
    public ApiResponse<Void> execute(AdminChangeStatusContractRequest request) {
        final Contract contract = this.contractRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy hợp đồng với id = {}.", request.getId()));

        User.currentUserId()
                .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

        final Contract.Status currentStatus = contract.getStatus();
        final Contract.Status nextStatus = request.getStatus();

        if (!AdminChangeStatusContractCommand.nextStatusesMapToCurrentStatus.containsKey(currentStatus)) {
            throw ApiException.badRequest("Không thể thay đổi trạng thái của hợp đồng này.");
        }
        final Set<Contract.Status> availableStatusesToChange = AdminChangeStatusContractCommand.nextStatusesMapToCurrentStatus.get(currentStatus);
        if (!availableStatusesToChange.contains(nextStatus)) {
            throw ApiException.badRequest("Trạng thái cần thay đổi phải là một trong những trạng thái sau đây: {}.", availableStatusesToChange);
        }

        User houseOwner = this.userRepository.findById(contract.getCreatorId())
                .orElseThrow(() -> ApiException.badRequest("Không tìm thấy người tạo đơn"));

        Contract contractToChange = contract.toBuilder().status(request.getStatus()).expiredAt(new Timestamp(System.currentTimeMillis())).build();

        final User userRemoveHouseOwner = removeRoleHouseOwner(houseOwner);

        Set<Accommodation> accommodationList = contract.getHouseOwner().getAccommodations();
        accommodationList.forEach(accommodation -> accommodation.setStatus(Accommodation.Status.BANNED));

        this.accommodationRepository.saveAll(accommodationList);
        this.contractRepository.save(contractToChange);
        this.userRepository.save(userRemoveHouseOwner);

        sendMail(contract);
        notificationService.send(notification -> notification.toUsers(userRemoveHouseOwner.getId()).withContent("Hợp đồng chủ nhà của bạn đã bị chấm dứt bởi quản trị viên."));

        return ApiResponse.success("Thay đổi trạng thái hợp đồng thành công.");
    }

    public void sendMail(Contract contract) {
        this.mailWorker.sendMail(
                mail -> mail
                        .sendTo(contract.getHouseOwner().getEmail())
                        .withTemplate(EmailTemplate.Key.CHANGE_CONTRACT_STATUS)
                        .withProperty("username", contract.getHouseOwner().getUsername())
                        .withProperty("contractName", contract.getName())
                        .withProperty("status", contract.getStatus())
                        .withSuccessMessage("Send mail successfully")
        );
    }

    public User removeRoleHouseOwner(final User user) {
        return user.toBuilder()
                .roles(user.getRoles().replaceAll(", " + User.Role.HOUSE_OWNER, ""))
                .build();
    }

    @Getter
    @Builder(toBuilder = true)
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class AdminChangeStatusContractRequest extends BaseRequest {
        @NonNull
        private final Integer id;
        @NonNull
        private final Contract.Status status;
    }
}
