package com.fpt.h2s.services.commands.transactions;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.BookingRequestRepository;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class CancelBookingRequestCommand implements BaseCommand<Integer, Void> {
    private final BookingRequestRepository bookingRequestRepository;

    @Override
    public ApiResponse<Void> execute(Integer id) {
        BookingRequest bookingRequest = bookingRequestRepository.getById(id, "Không tìm thấy yêu cầu đặt phòng");
        if (bookingRequest.getStatus() != BookingRequest.Status.PENDING) {
            throw ApiException.badRequest("Không thể huỷ yêu cầu này");
        }

        Integer currentId = User.getCurrentId();
        if (!bookingRequest.getCreatorId().equals(currentId)) {
            throw ApiException.forbidden();
        }
        BookingRequest bookingToSave = bookingRequest.withStatus(BookingRequest.Status.UN_PURCHASED);
        bookingRequestRepository.save(bookingToSave);

        String lastTransactionKey = CreateBookingTransactionRequestCommand.getLastTransactionKey(currentId);
        RedisRepository.get(lastTransactionKey).ifPresent(RedisRepository::remove);
        RedisRepository.remove(lastTransactionKey);

        log.info("Cancel booking request with id {} successfully", id);
        return ApiResponse.success();
    }
}
