package com.programmers.kdt.payment.dto;

public record PartialRefundPaymentRequest(
        Long amount,
        String reason
) {
}
