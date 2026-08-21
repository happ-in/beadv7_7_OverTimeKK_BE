package com.programmers.kdt.performance.dto;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSeatPrice;
import com.programmers.kdt.performance.entity.PerformanceSession;

import java.util.List;

public record RegisterPerformanceRequest(
        PerformanceV2Request performanceRequest,
        List<RegisterPerformanceSessionRequest> sessionRequests,
        List<PerformanceSeatPriceRequest> seatPriceRequests
) {
    public Performance toPerformance(Long sellerId) {
        return performanceRequest.toPerformance(sellerId);
    }

    public List<PerformanceSession> toSessions(Performance performance) {
        if (sessionRequests.isEmpty()) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE, "회차정보");
        }
        return sessionRequests.stream().map(session -> session.toSession(performance)).toList();
    }

    public List<PerformanceSeatPrice> toSeatPrices(Performance performance) {
        if (seatPriceRequests.isEmpty()) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE, "좌석금액 정보");
        }
        return seatPriceRequests.stream().map(seatPrice -> seatPrice.toSeatPrice(performance)).toList();
    }
}
