package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.dto.RegisterPerformanceResponse;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    public RegisterPerformanceResponse registerPerformance(RegisterPerformanceRequest request) {
        // TODO
        return null;
    }

    public void cancelPerformance(Long perfomanceInfoId) {
        // TODO: order-service에 REST로 취소/환불 요청 (OrderClient 필요)
    }
}
