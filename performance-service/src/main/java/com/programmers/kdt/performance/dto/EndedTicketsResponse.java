package com.programmers.kdt.performance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record EndedTicketsResponse(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        List<EndedTicketResponse> endedTickets
) {
}
