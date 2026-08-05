package com.programmers.kdt.settlement.dto;

public record SessionSettlementAmount(
        Long performanceId,
        Long sessionNum,
        Long grossAmount,
        Long pgFeeAmount,
        Long serviceFeeAmount
) {
}