package com.programmers.kdt.order.order.dto;

public record CreateOrderResponse(
        Long orderId,
        String orderStatus
) {
}
