package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.PerformanceSessionRequest;
import com.programmers.kdt.performance.dto.PerformanceSessionResponse;

import java.util.List;

public interface PerformanceSessionService {
    PerformanceSessionResponse registerPerformanceSession(PerformanceSessionRequest request);

    PerformanceSessionResponse changePerformanceSession(PerformanceSessionRequest request);

    void deletePerformanceSession(Long sessionNum, Long performanceId);

    void deletePerformanceSessions(Long performanceId);

    List<PerformanceSessionResponse> findAllPerformanceSessionsByPerformanceId(Long performanceId);

    PerformanceSessionResponse getPerformanceSession(Long sessionNum, Long performanceId);
}

