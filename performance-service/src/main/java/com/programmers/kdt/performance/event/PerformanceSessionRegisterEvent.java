package com.programmers.kdt.performance.event;

import com.programmers.kdt.performance.dto.RegisterPerformanceSessionRequest;
import com.programmers.kdt.performance.entity.Performance;

import java.util.List;

public record PerformanceSessionRegisterEvent(Performance performance, List<RegisterPerformanceSessionRequest> sessionRequests) {
}