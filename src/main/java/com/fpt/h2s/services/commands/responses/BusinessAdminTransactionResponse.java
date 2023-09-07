package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Transaction;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
public class BusinessAdminTransactionResponse {
    private Integer id;

    private Long amount;

    private String paymentMethod;

    private String transactionRequestNo;

    private String bankTransactionNo;

    private String payDate;

    private BusinessAdminTransactionResponse.UserResponse creator;

    private BusinessAdminTransactionResponse.UserResponse receiver;

    private Timestamp createdAt;

    private Integer bookingRequestId;

    public static BusinessAdminTransactionResponse of(final Transaction transaction) {
        return BusinessAdminTransactionResponse
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

    @Builder
    @Getter
    @With
    public static class UserResponse {
        private Integer id;

        private String email;

        private String username;

        private String phone;

        private List<User.Role> roles;

        private User.Status status;

        private Timestamp createdAt;

        private Timestamp updatedAt;

        public static BusinessAdminTransactionResponse.UserResponse of(final User user) {
            return Mappers.convertTo(BusinessAdminTransactionResponse.UserResponse.class, user).withRoles(user.roleList());
        }

    }
}
