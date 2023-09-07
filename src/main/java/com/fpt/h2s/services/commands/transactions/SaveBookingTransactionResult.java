package com.fpt.h2s.services.commands.transactions;

import ananta.utility.StringEx;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Accommodation;
import com.fpt.h2s.models.entities.BookingRequest;
import com.fpt.h2s.models.entities.Transaction;
import com.fpt.h2s.models.entities.TravelStatement;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.*;
import com.fpt.h2s.services.NotificationService;
import com.fpt.h2s.services.commands.BaseCommand;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Log4j2
@Transactional
@RequiredArgsConstructor
@Service
public class SaveBookingTransactionResult implements BaseCommand<SaveBookingTransactionResult.Request, Void> {

    private final TransactionRepository transactionRepository;

    private final TravelStatementRepository travelStatementRepository;

    private final BookingRequestRepository bookingRequestRepository;

    private final AccommodationRepository accommodationRepository;
    private final NotificationService notificationService;

    @Override
    public ApiResponse<Void> execute(final Request request) {
        final String redisToken = StringEx.format("tr-code-{}", request.id);
        final HashMap<String, Integer> payload = RedisRepository.get(redisToken, new TypeReference<HashMap<String, Integer>>() {
        }).orElseThrow();
        final Integer creatorId = payload.get(Transaction.Fields.creatorId);
        final Integer bookingId = payload.get("bookingId");

        if (!request.getVnp_ResponseCode().equals("00")) {
            String lastTransactionKey = CreateBookingTransactionRequestCommand.getLastTransactionKey(creatorId);
            RedisRepository.get(lastTransactionKey).ifPresent(RedisRepository::remove);
            RedisRepository.remove(lastTransactionKey);
            RedisRepository.remove(redisToken);
            throw ApiException.badRequest("Đặt phòng thất bại");
        }

        if (payload.containsKey("statementId")) {
            Integer statementId = payload.get("statementId");
            attachBookingRequestToStatement(bookingId, statementId);
        }

        Transaction transaction = this.transactionRepository.save(toTransaction(request, creatorId));

        BookingRequest bookingRequest = bookingRequestRepository.getById(bookingId, "Không thể tìm thấy yêu cầu đặt phòng.", bookingId);
        BookingRequest bookingToUpdate = bookingRequest.withStatus(BookingRequest.Status.PURCHASED).withTransactionId(transaction.getId());
        bookingRequestRepository.save(bookingToUpdate);

        Accommodation accommodation = bookingRequest.getAccommodation();
        accommodationRepository.save(accommodation.withTotalBookings(accommodation.getTotalBookings() + 1));

        notificationService.send(notification -> notification
            .toUser(accommodation.getOwnerId())
            .withContent("Nhà của bạn vừa có yêu cầu đặt phòng thành công.")
        );

        RedisRepository.remove(redisToken);
        return ApiResponse.success();
    }

    private void attachBookingRequestToStatement(Integer bookingId, Integer statementId) {
        try {
            TravelStatement statement = travelStatementRepository.getById(statementId, "Không thể tìm thấy tờ trình với id = {}.", statementId);
            TravelStatement statementToSave = statement.withBookingRequestId(bookingId);
            travelStatementRepository.save(statementToSave);
        } catch (Exception e) {
            log.warn("Save booking id for statement failed");
        }
    }

    private static Transaction toTransaction(Request request, Integer creatorId) {
        return Transaction
            .builder()
            .amount(request.vnp_Amount)
            .paymentMethod(request.vnp_BankCode)
            .payDate(request.vnp_PayDate)
            .creatorId(creatorId)
            .bankTransactionNo(request.vnp_BankTranNo)
            .transactionRequestNo(request.id)
            .build();
    }

    @Builder
    @Getter
    @FieldNameConstants
    @Jacksonized
    @With
    public static class Request {
        private final String id;

        private final Long vnp_Amount;
        private final String vnp_BankCode;
        private final String vnp_BankTranNo;
        private final String vnp_CardType;
        private final String vnp_OrderInfo;
        private final String vnp_PayDate;
        private final String vnp_ResponseCode;
        private final String vnp_TmnCode;
        private final String vnp_TransactionNo;
        private final String vnp_TransactionStatus;
        private final String vnp_TxnRef;
        private final String vnp_SecureHash;
    }
}
