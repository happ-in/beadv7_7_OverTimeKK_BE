package com.programmers.kdt.order.dto;

public record TicketReserveRequest(
        Long ticketId,
        String holdKey,
        Long userId
) {
}
