package com.programmers.kdt.order.dto;

import java.util.List;

public record OrderTicketRequest(
        List<Long> ticketIds
) {
}
