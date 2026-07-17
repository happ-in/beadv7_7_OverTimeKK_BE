package com.programmers.kdt.payment.dto;

public record CreatePaymentResponse(
        Long paymentId,
        String status
) {
}
