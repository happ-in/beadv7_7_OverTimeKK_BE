package com.programmers.kdt.performance.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.performance.dto.PerformanceRequest;
import com.programmers.kdt.performance.dto.PerformanceResponse;
import com.programmers.kdt.performance.service.PerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    // TODO: user-service 판매자 검증필요
    @PostMapping
    public ResponseEntity<ApiResponse<PerformanceResponse>> registerPerformance(
            @Valid @RequestBody PerformanceRequest request,
            @RequestHeader("X-User-Id") Long sellerId) {
        PerformanceResponse res = performanceService.registerPerformance(request, sellerId);
        return ResponseEntity.created(URI.create("/api/performances/" + res.performanceId())).body(ApiResponse.success(res));
    }

    @PutMapping("/{performanceId}")
    public ResponseEntity<ApiResponse<PerformanceResponse>> updatePerformance(
            @PathVariable Long performanceId,
            @Valid @RequestBody PerformanceRequest request,
            @RequestHeader("X-User-Id") Long sellerId) {
        PerformanceResponse res = performanceService.updatePerformance(performanceId, request, sellerId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/{performanceId}/cancel")
    public void cancel(@PathVariable Long performanceId) {
        performanceService.cancelPerformance(performanceId);
    }
}
