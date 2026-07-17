package com.programmers.kdt.payment.dto;

public record CreatePaymentRequest(
        Long orderId,
        Long amount
) {
}
