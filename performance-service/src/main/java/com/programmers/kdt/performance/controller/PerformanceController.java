package com.programmers.kdt.performance.controller;

import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.dto.RegisterPerformanceResponse;
import com.programmers.kdt.performance.service.PerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    public RegisterPerformanceResponse register(@Valid @RequestBody RegisterPerformanceRequest request) {
        return performanceService.registerPerformance(request);
    }

    @PostMapping("/{perfomanceInfoId}/cancel")
    public void cancel(@PathVariable Long perfomanceInfoId) {
        performanceService.cancelPerformance(perfomanceInfoId);
    }
}
