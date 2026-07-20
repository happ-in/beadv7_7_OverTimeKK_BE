package com.programmers.kdt.performance.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.dto.RegisterPerformanceResponse;
import com.programmers.kdt.performance.dto.UpdatePerformanceRequest;
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
    public ResponseEntity<ApiResponse<RegisterPerformanceResponse>> register(
            @Valid @RequestBody RegisterPerformanceRequest request,
            @RequestHeader("X-User-Id") Long sellerId) {
        RegisterPerformanceResponse res = performanceService.registerPerformance(request, sellerId);
        return ResponseEntity.created(URI.create("/api/performances/" + res.performanceId())).body(ApiResponse.success(res));
    }

    @PutMapping("/{performanceId}")
    public ApiResponse<Void> updatePerformance(
            @PathVariable Long performanceId,
            @Valid @RequestBody UpdatePerformanceRequest request,
            @RequestHeader("X-User-Id") Long sellerId) {
        performanceService.updatePerformance(performanceId, request, sellerId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{performanceId}/cancel")
    public void cancel(@PathVariable Long performanceId) {
        performanceService.cancelPerformance(performanceId);
    }
}
