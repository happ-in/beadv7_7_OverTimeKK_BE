package com.programmers.kdt.settlement.dto;

public record GetTicketsResponse(
        Long ticketId,
        Long performanceId,
        Long sessionNum
) {
}
