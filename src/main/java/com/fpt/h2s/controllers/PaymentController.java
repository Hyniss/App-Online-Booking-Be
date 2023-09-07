package com.fpt.h2s.controllers;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Transaction;
import com.fpt.h2s.repositories.TransactionRepository;
import com.fpt.h2s.services.BankingService;
import com.fpt.h2s.services.PaymentService;
import com.fpt.h2s.services.VNPayService;
import com.fpt.h2s.services.commands.requests.Payment;
import com.fpt.h2s.utilities.SpringBeans;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    @PostMapping("/vnpay/refund")
    public void refund(Integer id, String username) {
        Transaction transaction = SpringBeans.getBean(TransactionRepository.class).getById(id);
        PaymentService.RefundRequest request = PaymentService.RefundRequest
            .builder()
            .percentage(100)
            .username(username)
            .transaction(transaction)
            .build();
        this.paymentService.refund(request);
    }

    @PostMapping("/status")
    public ApiResponse<BankingService.Purchase.Status> checkStatus(@RequestBody final Payment.PurchaseStatusRequest request) {
        return ApiResponse.success(this.vnPayService.checkPaymentStatus(request));
    }

    @GetMapping("/vnpay/success")
    public String vnPaySuccess() {
        return "Ok";
    }
}
