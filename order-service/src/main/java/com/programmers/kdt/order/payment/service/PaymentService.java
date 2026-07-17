package com.programmers.kdt.order.payment.service;

import com.programmers.kdt.order.payment.dto.CreatePaymentRequest;
import com.programmers.kdt.order.payment.dto.CreatePaymentResponse;
import com.programmers.kdt.order.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public CreatePaymentResponse pay(CreatePaymentRequest request) {
        // TODO: PG 연동
        return null;
    }

    public void cancelPayment(Long paymentId) {
        // TODO
    }
}
