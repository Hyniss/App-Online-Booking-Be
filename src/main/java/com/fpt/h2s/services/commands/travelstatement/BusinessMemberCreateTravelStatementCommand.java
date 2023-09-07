package com.fpt.h2s.services.commands.travelstatement;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.TravelStatement;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.TravelStatementRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.BusinessMemberCreateTravelStatementRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class BusinessMemberCreateTravelStatementCommand implements BaseCommand<BusinessMemberCreateTravelStatementRequest, TravelStatement> {

    private final TravelStatementRepository travelStatementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    @Override
    public ApiResponse<TravelStatement> execute(BusinessMemberCreateTravelStatementRequest request) {
        TravelStatement travelStatement = request.toTravelStatement();
        travelStatement = this.travelStatementRepository.save(travelStatement);

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));

        List<Integer> businessAdminIds = userRepository.findAllUsersByRole(User.Role.BUSINESS_ADMIN.name()).stream().filter(businessAdmin -> Objects.equals(businessAdmin.getCompanyId(), currentUser.getCompanyId())).map(User::getId).toList();
        final String travelStatementName = travelStatement.getName();
        notificationService.send(notification -> notification.toUsers(businessAdminIds).withContent("Có một đơn mới " + travelStatementName + " đã được tạo"));
        return ApiResponse.success("Tạo tờ trình thành công.", travelStatement);
    }
}
