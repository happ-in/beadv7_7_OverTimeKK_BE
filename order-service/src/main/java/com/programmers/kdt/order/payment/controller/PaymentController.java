package com.programmers.kdt.order.payment.controller;

import com.programmers.kdt.order.payment.dto.CreatePaymentRequest;
import com.programmers.kdt.order.payment.dto.CreatePaymentResponse;
import com.programmers.kdt.order.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public CreatePaymentResponse pay(@Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.pay(request);
    }

    @PostMapping("/{paymentId}/cancel")
    public void cancel(@PathVariable Long paymentId) {
        paymentService.cancelPayment(paymentId);
    }
}
