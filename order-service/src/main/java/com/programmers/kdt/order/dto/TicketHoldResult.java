package com.programmers.kdt.order.dto;

import java.time.LocalDateTime;

public record TicketHoldResult(
        Long ticketId,
        Long price,
        LocalDateTime holdExpiredAt,
        String holdKey
) {
}
