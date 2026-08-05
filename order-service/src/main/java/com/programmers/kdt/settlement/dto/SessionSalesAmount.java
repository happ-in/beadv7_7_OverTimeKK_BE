package com.programmers.kdt.settlement.dto;

public record SessionSalesAmount(
        Long performanceId,
        Long sessionNum,
        Long grossAmount
) {
}
