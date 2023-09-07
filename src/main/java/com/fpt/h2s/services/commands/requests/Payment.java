package com.fpt.h2s.services.commands.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.services.BankingService;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;

public class Payment {

    @Builder
    @Getter
    @FieldNameConstants
    @Jacksonized
    @With
    public static class PurchaseRequest extends BaseRequest implements BankingService.Purchase.Request {
        private PurchaseType type;
        private Long amount;
        private String bankCode;
        private DisplayLanguage displayLanguage;
        private String returnUrl;
        private String transactionId;
    }

    @Getter
    @Builder
    public static class PurchaseResponse implements BankingService.Purchase.Response {
        private String id;
        private String url;

        @Override
        @JsonIgnore
        public String getTransactionId() {
            return id;
        }
    }

    @Getter
    @Builder
    public static class PurchaseStatusRequest implements BankingService.Purchase.StatusRequest {
        private String transactionNo;
        private String orderId;
        private String transactionDate;

    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class TransferMoneyRequest implements BankingService.Transfer.Request {
        private String bankCode;
        private String accountNo;
        private String accountType;
        private String accountName;
        private Long amount;
        private String message;
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class TransferMoneyResponse implements BankingService.Transfer.Response {
        private String errorCode;
        private String message;
        private String orderId;
        private String amount;
        private String paymentUrl;
        private String signature;

        @Override
        public String getTransactionId() {
            return orderId;
        }

        @Override
        public String getStatusCode() {
            return errorCode;
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    public static class RefundRequest implements BankingService.Refund.Request {
        private RefundType type;
        private Long amount;
        private String bankCode;
        private DisplayLanguage locate;
        private String orderId;
        private String transactionDate;
        private String user;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class InformationRequest implements BankingService.CardDetail.CheckRequest {
        private String bankCode;
        private String accountNo;
        private String accountType;
    }

    @AllArgsConstructor
    @Getter
    public enum PurchaseType {
        ATM("ATM");

        private final String value;
    }

    @AllArgsConstructor
    @Getter
    public enum RefundType {
        REFUND_FULL("02"),
        REFUND_PART("03");

        private final String value;
    }

    @AllArgsConstructor
    @Getter
    public enum BankType {
        NCB("NCB"),
        MASTERCARD("MASTERCARD");

        private final String value;
    }

    @AllArgsConstructor
    @Getter
    public enum DisplayLanguage {
        VIETNAMESE("vn"),
        ENGLISH("en");

        private final String value;

    }
}
