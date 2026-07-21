package com.programmers.kdt.performance.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.performance.dto.PerformanceDetailResponse;
import com.programmers.kdt.performance.dto.PerformanceRequest;
import com.programmers.kdt.performance.dto.PerformanceResponse;

import java.util.List;
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
    public ApiResponse<PerformanceResponse> updatePerformance(
            @PathVariable Long performanceId,
            @Valid @RequestBody PerformanceRequest request,
            @RequestHeader("X-User-Id") Long sellerId) {
        return ApiResponse.success(performanceService.updatePerformance(performanceId, request, sellerId));
    }

    @GetMapping("/{performanceId}")
    public ApiResponse<PerformanceDetailResponse> getPerformance(@PathVariable Long performanceId) {
        return ApiResponse.success(performanceService.getPerformanceDetail(performanceId));
    }

    @GetMapping
    public ApiResponse<List<PerformanceDetailResponse>> getPerformances() {
        return ApiResponse.success(performanceService.getPerformances());
    }

    @PostMapping("/{performanceId}/cancel")
    public void cancel(@PathVariable Long performanceId) {
        performanceService.cancelPerformance(performanceId);
    }

    @DeleteMapping("/{performanceId}")
    public ResponseEntity<Void> deletePerformance(
            @PathVariable Long performanceId,
            @RequestHeader("X-User-Id") Long sellerId) {
        performanceService.deletePerformance(performanceId, sellerId);
        return ResponseEntity.noContent().build();
    }
}
