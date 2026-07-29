package com.programmers.kdt.order.event;

public record TicketCancelRequestEvent(
        Long ticketId,
        Long userId,
        Long orderId
) {
}
