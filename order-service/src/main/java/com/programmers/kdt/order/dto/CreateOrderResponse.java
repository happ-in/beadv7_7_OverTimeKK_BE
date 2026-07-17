package com.programmers.kdt.order.dto;

public record CreateOrderResponse(
        Long orderId,
        String orderStatus
) {
}
