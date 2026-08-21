package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.PerformanceSessionRequest;
import com.programmers.kdt.performance.dto.PerformanceSessionResponse;

import java.util.List;

public interface PerformanceSessionService {
    PerformanceSessionResponse registerPerformanceSession(PerformanceSessionRequest request, Long sellerId);

    PerformanceSessionResponse changePerformanceSession(PerformanceSessionRequest request, Long sellerId);

    void deletePerformanceSession(Long sessionNum, Long performanceId, Long sellerId);

    void deletePerformanceSessions(Long performanceId, Long sellerId);

    List<PerformanceSessionResponse> findAllPerformanceSessionsByPerformanceId(Long performanceId);

    PerformanceSessionResponse getPerformanceSession(Long sessionNum, Long performanceId);
}

