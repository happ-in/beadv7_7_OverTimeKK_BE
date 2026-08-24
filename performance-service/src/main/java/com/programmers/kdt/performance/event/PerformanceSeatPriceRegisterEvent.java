package com.programmers.kdt.performance.event;

import com.programmers.kdt.performance.dto.PerformanceSeatPriceRequest;
import com.programmers.kdt.performance.entity.Performance;

import java.util.List;

public record PerformanceSeatPriceRegisterEvent(Performance performance, List<PerformanceSeatPriceRequest> seatPriceRequests) {
}