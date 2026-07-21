package com.programmers.kdt.payment.dto;

public record PartialCancelPaymentRequest(
        Long amount,
        String reason
) {
}
