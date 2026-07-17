package com.programmers.kdt.order.order.dto;

public record CreateOrderRequest(
        Long userId,
        Long ticketId
) {
}
