package com.programmers.kdt.order.dto;

public record CreateOrderRequest(
        Long userId,
        Long ticketId
) {
}
