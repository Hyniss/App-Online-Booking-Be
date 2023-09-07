package com.fpt.h2s.services;

import ananta.utility.StringEx;
import com.fpt.h2s.models.entities.Transaction;
import com.fpt.h2s.services.commands.requests.Payment;
import com.fpt.h2s.utilities.MoreStrings;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VNPayService vnPayService;
    private final AppotaPaymentService appotaPaymentService;

    public BankingService.Purchase.Response createPayRequest(final Long amount, String returnUrl) {
        final String transactionId = MoreStrings.randomNumber(12);
        Payment.PurchaseRequest request = Payment.PurchaseRequest.builder()
            .transactionId(transactionId)
            .returnUrl(StringEx.format(returnUrl, transactionId))
            .amount(amount)
            .displayLanguage(Payment.DisplayLanguage.VIETNAMESE)
            .build();
        return vnPayService.createPayRequest(request);
    }

    @SneakyThrows
    public void refund(RefundRequest refundRequest) {
        Payment.RefundRequest request = new Payment.RefundRequest();
        Transaction transaction = refundRequest.getTransaction();
        request.setAmount(transaction.getAmount() * refundRequest.percentage / 100);
        request.setType(refundRequest.percentage == 100 ? Payment.RefundType.REFUND_FULL : Payment.RefundType.REFUND_PART);
        request.setUser(refundRequest.username);
        request.setBankCode(transaction.getPaymentMethod());
        request.setOrderId(transaction.getTransactionRequestNo());
        request.setTransactionDate(transaction.getPayDate());
        request.setLocate(Payment.DisplayLanguage.VIETNAMESE);
        vnPayService.refund(request);
    }

    public Payment.TransferMoneyResponse createTransferRequest(Payment.TransferMoneyRequest request) {
        return (Payment.TransferMoneyResponse) appotaPaymentService.createTransferRequest(request);
    }

    public boolean isCardValid(BankingService.CardDetail.CheckRequest request) {
        return true;
    }

    @Builder
    @Getter
    public static class RefundRequest {
        private Transaction transaction;
        private Integer percentage;
        private String username;
    }

}
