package com.programmers.kdt.order.client;

public record TicketInfo(
        Long ticketId,
        String performanceName,
        String zone
) {
}
