package com.fpt.h2s.services;

public interface BankingService {

    Purchase.Response createPayRequest(Purchase.Request request);
    Purchase.Status checkPaymentStatus(Purchase.StatusRequest request);

    void refund(Refund.Request request);

    Transfer.Response createTransferRequest(Transfer.Request request);

    boolean isCardValid(CardDetail.CheckRequest request);

    class Purchase {
        public interface Request {
            String getBankCode();
            Long getAmount();
        }
        public interface Response {
            String getTransactionId();
            String getUrl();
        }

        public interface StatusRequest {
            String getTransactionNo();

            String getOrderId();

            String getTransactionDate();
        }
        public enum Status {
            PENDING, FAILED, SUCCEED
        }
    }

    class Refund {
        public interface Request {
            Long getAmount();
        }
    }

    class Transfer {
        public interface Request {
            String getBankCode();
            String getAccountNo();
            String getAccountType();
            Long getAmount();
        }
        public interface Response {
            String getTransactionId();
            String getStatusCode();
            String getMessage();
        }
    }

    class CardDetail {
        public interface CheckRequest {
            String getBankCode();
            String getAccountNo();
            String getAccountType();
        }
    }
}
