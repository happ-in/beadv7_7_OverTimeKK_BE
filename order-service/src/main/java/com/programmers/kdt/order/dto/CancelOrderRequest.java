package com.programmers.kdt.order.dto;

import jakarta.validation.constraints.NotNull;

public record CancelOrderRequest(
        String reason
) {
}
