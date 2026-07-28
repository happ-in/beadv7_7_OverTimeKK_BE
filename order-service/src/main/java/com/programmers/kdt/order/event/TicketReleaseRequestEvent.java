package com.programmers.kdt.order.event;

public record TicketReleaseRequestEvent(
        Long orderId,
        Long ticketId,
        String holdKey
) {
}
