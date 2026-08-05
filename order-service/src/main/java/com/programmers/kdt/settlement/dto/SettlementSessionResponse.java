package com.programmers.kdt.settlement.dto;

public record SettlementSessionResponse(
        Long performanceId,
        Long sessionNum,
        Long sellerId
) {
}
