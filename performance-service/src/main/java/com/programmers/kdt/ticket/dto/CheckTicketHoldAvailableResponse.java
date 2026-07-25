package com.programmers.kdt.ticket.dto;

import java.time.LocalDateTime;

public record CheckTicketHoldAvailableResponse(
        Long ticketId,
        Long price,
        LocalDateTime holdExpiredAt
) {
}
