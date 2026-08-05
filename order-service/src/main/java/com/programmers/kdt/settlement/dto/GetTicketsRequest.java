package com.programmers.kdt.settlement.dto;

public record GetTicketsRequest(
        Long performanceId,
        Long sessionNum
) {
}
