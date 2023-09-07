package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.ChangeStatusAccommodationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.fpt.h2s.models.entities.User.Role.ADMIN;
import static com.fpt.h2s.models.entities.User.Role.HOUSE_OWNER;

@Service
@RequiredArgsConstructor
public class ChangeStatusAccommodationCommand implements BaseCommand<ChangeStatusAccommodationRequest, Void> {
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final Map<Accommodation.Status, Set<Accommodation.Status>> nextStatusesMapToCurrentStatusForSystemAdmin = Map.ofEntries(
            Map.entry(Accommodation.Status.PENDING, Set.of(Accommodation.Status.OPENING, Accommodation.Status.REJECTED)),
            Map.entry(Accommodation.Status.OPENING, Set.of(Accommodation.Status.BANNED)),
            Map.entry(Accommodation.Status.BANNED, Set.of(Accommodation.Status.OPENING))
    );

    private static final Map<Accommodation.Status, Set<Accommodation.Status>> nextStatusesMapToCurrentStatusForHouseOwner = Map.ofEntries(
            Map.entry(Accommodation.Status.OPENING, Set.of(Accommodation.Status.CLOSED)),
            Map.entry(Accommodation.Status.CLOSED, Set.of(Accommodation.Status.OPENING))
    );

    @Override
    public ApiResponse<Void> execute(ChangeStatusAccommodationRequest request) {
        final Accommodation accommodation = this.accommodationRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy chỗ ở với id = {}.", request.getId()));

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));

        final Accommodation.Status currentStatus = accommodation.getStatus();
        final Accommodation.Status nextStatus = request.getStatus();

        //Change status accommodation for business admin
        if(currentUser.is(ADMIN)) {
            if (!ChangeStatusAccommodationCommand.nextStatusesMapToCurrentStatusForSystemAdmin.containsKey(currentStatus)) {
                throw ApiException.badRequest("Không thể thay đổi trạng thái của chỗ ở.");
            }

            final Set<Accommodation.Status> availableStatusesToChange = ChangeStatusAccommodationCommand.nextStatusesMapToCurrentStatusForSystemAdmin.get(currentStatus);
            if (!availableStatusesToChange.contains(nextStatus)) {
                throw ApiException.badRequest("Trạng thái cần thay đổi phải là một trong những trạng thái sau đây: {}.", availableStatusesToChange);
            }
        }

        //Change status accommodation for house owner
        else if(currentUser.is(HOUSE_OWNER)) {
            if(!Objects.equals(accommodation.getCreatorId(), currentUserId)) {
                throw ApiException.badRequest("Không thể thay đổi trạng thái của chỗ ở..");
            }

            if (!ChangeStatusAccommodationCommand.nextStatusesMapToCurrentStatusForHouseOwner.containsKey(currentStatus)) {
                throw ApiException.badRequest("Không thể thay đổi trạng thái của chỗ ở.");
            }

            final Set<Accommodation.Status> availableStatusesToChange = ChangeStatusAccommodationCommand.nextStatusesMapToCurrentStatusForHouseOwner.get(currentStatus);
            if (!availableStatusesToChange.contains(nextStatus)) {
                throw ApiException.badRequest("Trạng thái cần thay đổi phải là một trong những trạng thái sau đây: {}", availableStatusesToChange);
            }
        }

        //xu ly logic ban/close o day
        Accommodation accommodationToChange = accommodation.toBuilder().status(request.getStatus()).build();
        this.accommodationRepository.save(accommodationToChange);

        if(currentUser.is(ADMIN)) {
            notificationService.send(notification -> notification.toUsers(accommodationToChange.getOwnerId()).withContent("Trạng thái chỗ ở của bạn " + accommodationToChange.getName() + " đã được thay đổi bởi quản trị viên."));
        }

        return ApiResponse.success("Thay đổi trạng thái chỗ ở thành công.");
    }
}
