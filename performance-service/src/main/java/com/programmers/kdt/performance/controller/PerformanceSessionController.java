package com.programmers.kdt.performance.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.performance.dto.PerformanceSessionRequest;
import com.programmers.kdt.performance.dto.PerformanceSessionResponse;
import com.programmers.kdt.performance.service.PerformanceSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/performance")
public class PerformanceSessionController {

    private final PerformanceSessionService performanceSessionService;

    // TODO : 회차정보 작성도 회원 인증(판매자 검증) 필요.
    @PostMapping("/session")
    public ApiResponse<?> registerPerformanceSession(@Valid @RequestBody PerformanceSessionRequest request) {
        PerformanceSessionResponse response = performanceSessionService.registerPerformanceSession(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/session")
    public ApiResponse<?> changePerformanceSession(@Valid @RequestBody PerformanceSessionRequest request) {
        PerformanceSessionResponse response = performanceSessionService.changePerformanceSession(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{performanceId}/session")
    public ApiResponse<?> getPerformanceSessions(@Valid @PathVariable Long performanceId) {
        List<PerformanceSessionResponse> responses = performanceSessionService.findAllPerformanceSessionsByPerformanceId(performanceId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{performanceId}/session/{sessionNum}")
    public ApiResponse<?> getPerformanceSession(@Valid @PathVariable Long performanceId, @PathVariable Long sessionNum) {
        PerformanceSessionResponse response = performanceSessionService.getPerformanceSession(sessionNum, performanceId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{performanceId}/session/{sessionNum}")
    public ApiResponse<?> deletePerformanceSession(@Valid @PathVariable Long performanceId, @PathVariable Long sessionNum) {
        performanceSessionService.deletePerformanceSession(sessionNum, performanceId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{performanceId}/session")
    public ApiResponse<?> deletePerformanceSessions(@PathVariable Long performanceId) {
        performanceSessionService.deletePerformanceSessions(performanceId);
        return ApiResponse.success(null);
    }
}
