package com.programmers.kdt.order.dto;

public record TicketCancelRequest(
        Long ticketId,
        Long userId
) {
}
