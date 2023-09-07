package com.fpt.h2s.services.commands.boookingrequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.BookingRequestRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.BusinessOwnerDetailBookingRequestResponse;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BusinessOwnerViewDetailedBookingRequestCommand implements BaseCommand<BusinessOwnerViewDetailedBookingRequestCommand.BusinessOwnerViewDetailedBookingRequestRequest, BusinessOwnerDetailBookingRequestResponse> {
    private final UserRepository userRepository;
    private final BookingRequestRepository bookingRequestRepository;

    @Override
    public ApiResponse<BusinessOwnerDetailBookingRequestResponse> execute(final BusinessOwnerViewDetailedBookingRequestCommand.BusinessOwnerViewDetailedBookingRequestRequest request) {
        BookingRequest bookingRequest = this.bookingRequestRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không tìm thấy yêu cầu đặt phòng."));

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        User currentUser = this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));
        if(currentUser.is(User.Role.BUSINESS_OWNER) || currentUser.is(User.Role.BUSINESS_ADMIN)) {
            if (Objects.equals(bookingRequest.getUser().getCompanyId(), currentUser.getCompanyId())){
                final BusinessOwnerDetailBookingRequestResponse response = BusinessOwnerDetailBookingRequestResponse.of(bookingRequest);
                return ApiResponse.success(response);
            }
        }

        if(currentUser.is(User.Role.BUSINESS_MEMBER) && bookingRequest.getTravelStatement() != null){
            if (Objects.equals(bookingRequest.getTravelStatement().getCreatorId(), currentUserId)){
                final BusinessOwnerDetailBookingRequestResponse response = BusinessOwnerDetailBookingRequestResponse.of(bookingRequest);
                return ApiResponse.success(response);
            }
        }

        return ApiResponse.badRequest("Xảy ra một vài lỗi trong quá trình xử lý, vui lòng thử lại.");

    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class BusinessOwnerViewDetailedBookingRequestRequest extends BaseRequest {
        @NonNull
        private final Integer id;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<BusinessOwnerViewDetailedBookingRequestCommand.BusinessOwnerViewDetailedBookingRequestRequest> {

            @Override
            protected void validate() {
                this.rejectIfEmpty(BusinessOwnerViewDetailedBookingRequestCommand.BusinessOwnerViewDetailedBookingRequestRequest.Fields.id, this::validateBookingRequestId);
            }

            private String validateBookingRequestId() {
                return null;
            }
        }
    }
}
