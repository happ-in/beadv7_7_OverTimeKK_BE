package com.programmers.kdt.ticket.dto;

public record OrderTicketResponse(
        Long ticketId,
        String performanceName,
        String zone
) {
}
