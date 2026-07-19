package com.programmers.kdt.performance.controller;

import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.dto.RegisterPerformanceResponse;
import com.programmers.kdt.performance.service.PerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    public RegisterPerformanceResponse register(@Valid @RequestBody RegisterPerformanceRequest request) {
        return performanceService.registerPerformance(request);
    }

    @PostMapping("/{performanceId}/cancel")
    public void cancel(@PathVariable Long performanceId) {
        performanceService.cancelPerformance(performanceId);
    }
}
