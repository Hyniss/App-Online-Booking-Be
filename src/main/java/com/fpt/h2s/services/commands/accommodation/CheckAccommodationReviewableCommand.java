package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.AccommodationReviewRepository;
import com.fpt.h2s.repositories.BookingRequestRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckAccommodationReviewableCommand implements BaseCommand<CheckAccommodationReviewableCommand.Request, Boolean> {
    private final BookingRequestRepository bookingRequestRepository;

    private final AccommodationReviewRepository accommodationReviewRepository;

    @Override
    public ApiResponse<Boolean> execute(final Request request) {
        BookingRequest bookingRequest = bookingRequestRepository.getById(request.getBookingId());
        boolean bookedByMe = bookingRequest.isBookedBy(User.getCurrentId());
        if (!bookedByMe) {
            throw ApiException.forbidden("Bạn không có quyền truy cập để thực hiện tác vụ này.");
        }

        Timestamp monthAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
        boolean canBook = bookingRequest.getStatus() == BookingRequest.Status.SUCCEED &&
            bookingRequest.getUpdatedAt().after(monthAgo) &&
            !accommodationReviewRepository.existsByRequestId(bookingRequest.getId());
        return ApiResponse.success(canBook);
    }

    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static final class Request extends BaseRequest {
        private final Integer bookingId;
    }

    @With
    @Getter
    @Builder
    @FieldNameConstants
    @Jacksonized
    public static class BookingDetailResponse {
        private Integer bookingId;
        private final List<String> roomNames;
        private final Timestamp bookedAt;
    }

    public interface BookingRoomProjection {
        Integer getId();
        String getName();
        Timestamp getBookedAt();
    }

}
