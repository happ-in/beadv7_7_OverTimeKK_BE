package com.programmers.kdt.order.payment.dto;

public record CreatePaymentRequest(
        Long orderId,
        Long amount
) {
}
