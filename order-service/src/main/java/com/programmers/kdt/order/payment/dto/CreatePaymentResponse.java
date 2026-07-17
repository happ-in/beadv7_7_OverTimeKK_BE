package com.programmers.kdt.order.payment.dto;

public record CreatePaymentResponse(
        Long paymentId,
        String status
) {
}
