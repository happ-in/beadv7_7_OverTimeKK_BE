package com.programmers.kdt.performance.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.performance.dto.FindPerformanceResponse;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import com.programmers.kdt.performance.service.FindPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/find/performances")
@RequiredArgsConstructor
public class FindPerformanceController {

    private final FindPerformanceService findPerformanceService;

    @GetMapping
    public ApiResponse<List<FindPerformanceResponse>> findPerformanceByStatus(
            @RequestParam(name = "status", required = false) PerformanceStatus status,
            @RequestParam(name = "page", defaultValue = "1") int page) {
        List<FindPerformanceResponse> performances = findPerformanceService.findPerformancesByStatus(status, page);
        return ApiResponse.success(performances);
    }
}
