package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.FindPerformanceResponse;
import com.programmers.kdt.performance.entity.PerformanceStatus;

import java.util.List;

public interface FindPerformanceService {
    List<FindPerformanceResponse> findPerformances(PerformanceStatus status, int page);
}
