package com.programmers.kdt.performance.dto;

import com.programmers.kdt.performance.entity.Performance;

import java.util.List;

public record RegisterPerformanceRequest(
        PerformanceV2Request performanceRequest,
        List<RegisterPerformanceSessionRequest> sessionRequests,
        List<PerformanceSeatPriceRequest> seatPriceRequests
) {
    public Performance toPerformance(Long sellerId) {
        return performanceRequest.toPerformance(sellerId);
    }
}
