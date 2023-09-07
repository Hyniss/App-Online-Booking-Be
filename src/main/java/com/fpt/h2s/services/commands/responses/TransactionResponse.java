package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Transaction;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.sql.Timestamp;

@Builder
@Getter
@With
public class TransactionResponse {
    private Integer id;
    private Long amount;
    private String paymentMethod;
    private String transactionRequestNo;
    private String bankTransactionNo;
    private String payDate;

    private UserResponse creator;
    private UserResponse receiver;
    private Timestamp createdAt;
    private Integer bookingRequestId;

    public static TransactionResponse of(final Transaction transaction) {
        return TransactionResponse
                .builder()
                .id(transaction.getId())
                .amount(transaction.getAmount() / 100)
                .paymentMethod(transaction.getPaymentMethod())
                .transactionRequestNo(transaction.getTransactionRequestNo())
                .bankTransactionNo(transaction.getBankTransactionNo())
                .payDate(transaction.getPayDate())
                .creator((transaction.getCreator() == null) ? null : UserResponse.of(transaction.getCreator()))
                .receiver((transaction.getReceiver() == null) ? null : UserResponse.of(transaction.getReceiver()))
                .bookingRequestId((transaction.getBookingRequest() == null) ? null : transaction.getBookingRequest().getId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
