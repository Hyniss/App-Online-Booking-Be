package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.TravelStatement;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.TravelStatementRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.UserResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GetInformationOfBookingUserCommand implements BaseCommand<GetInformationOfBookingUserCommand.Request, UserResponse> {

    private final UserRepository userRepository;
    private final TravelStatementRepository travelStatementRepository;

    @Override
    public ApiResponse<UserResponse> execute(Request request) {
        User user = userRepository.getById(User.getCurrentId());
        if (user.is(User.Role.BUSINESS_ADMIN)) {
            if (request.statementId == null) {
                throw ApiException.badRequest("Xin hãy chọn tờ trình");
            }
            TravelStatement statement = travelStatementRepository.getById(request.statementId, "Không tìm thấy tờ trình");
            User statementOwner = statement.getUser();
            boolean isSameCompany = Objects.equals(statementOwner.getCompanyId(), user.getCompanyId());
            if (!isSameCompany) {
                throw ApiException.forbidden();
            }
            return ApiResponse.success(UserResponse.of(statementOwner));
        }

        return ApiResponse.success(UserResponse.of(user));
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class Request extends BaseRequest {

        private final Integer statementId;

    }
}
