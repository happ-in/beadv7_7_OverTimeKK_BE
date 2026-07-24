package com.programmers.kdt.order.client;

import java.time.LocalDateTime;

public record TicketHoldResult(
        Long ticketId,
        Long ticketPrice,
        LocalDateTime holdExpiresAt
) {
}
