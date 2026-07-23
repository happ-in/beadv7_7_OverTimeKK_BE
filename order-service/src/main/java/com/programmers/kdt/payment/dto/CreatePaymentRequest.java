package com.programmers.kdt.payment.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull Long orderId,
        @NotNull Long amount
) {
}
