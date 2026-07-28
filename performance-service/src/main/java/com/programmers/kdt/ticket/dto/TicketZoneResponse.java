package com.programmers.kdt.ticket.dto;

import com.programmers.kdt.ticket.entity.TicketStatus;

public record TicketZoneResponse(
        Long ticketId,
        String seatRow,
        String seatNum,
        TicketStatus ticketStatus
) {
}
