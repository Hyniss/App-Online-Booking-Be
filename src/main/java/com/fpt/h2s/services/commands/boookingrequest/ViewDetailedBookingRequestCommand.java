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
import com.fpt.h2s.services.commands.responses.DetailBookingRequestResponse;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ViewDetailedBookingRequestCommand implements BaseCommand<ViewDetailedBookingRequestCommand.ViewDetailedBookingRequestRequest, DetailBookingRequestResponse> {
    private final UserRepository userRepository;
    private final BookingRequestRepository bookingRequestRepository;

    @Override
    public ApiResponse<DetailBookingRequestResponse> execute(final ViewDetailedBookingRequestRequest request) {
        BookingRequest bookingRequest = this.bookingRequestRepository
                .findById(request.getId())
                .orElseThrow(() -> ApiException.badRequest("Không tìm thấy yêu cầu đặt phòng."));

        Integer currentUserId = User.currentUserId()
                .orElseThrow();

        this.userRepository
                .findById(currentUserId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", currentUserId));

        if (Objects.equals(bookingRequest.getUserId(), currentUserId) || Objects.equals(bookingRequest.getAccommodation().getCreatorId(), currentUserId)){
            final DetailBookingRequestResponse response = DetailBookingRequestResponse.of(bookingRequest);
            return ApiResponse.success(response);
        }

        return ApiResponse.badRequest("Xảy ra một vài lỗi trong quá trình xử lý, vui lòng thử lại.");

    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class ViewDetailedBookingRequestRequest extends BaseRequest {
        @NonNull
        private final Integer id;

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<ViewDetailedBookingRequestRequest> {

            @Override
            protected void validate() {
                this.rejectIfEmpty(ViewDetailedBookingRequestRequest.Fields.id, this::validateBookingRequestId);
            }

            private String validateBookingRequestId() {
                return null;
            }
        }
    }
}
