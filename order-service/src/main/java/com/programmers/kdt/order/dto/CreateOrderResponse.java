package com.programmers.kdt.order.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderResponse(
        @NotNull
        Long orderId,

        @NotNull
        String orderStatus
) {
}
