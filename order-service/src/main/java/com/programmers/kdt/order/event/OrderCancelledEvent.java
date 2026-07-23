package com.programmers.kdt.order.event;

public record OrderCancelledEvent(
        Long orderId,
        Long ticketId,
        Long userId
) {
}
