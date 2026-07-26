package com.programmers.kdt.performance.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.performance.dto.EndedTicketsResponse;
import com.programmers.kdt.performance.dto.FindPerformanceResponse;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import com.programmers.kdt.performance.service.FindPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class FindPerformanceController {

    private final FindPerformanceService findPerformanceService;

    @GetMapping
    public ApiResponse<List<FindPerformanceResponse>> findPerformances(
            @RequestParam(name = "status", required = false) PerformanceStatus status,
            @RequestParam(name = "page", defaultValue = "1") int page) {
        List<FindPerformanceResponse> performances = findPerformanceService.findPerformances(status, page);
        return ApiResponse.success(performances);
    }

    @GetMapping("/ended")
    public ApiResponse<EndedTicketsResponse> findEndedPerformanceTickets(
            @JsonFormat(pattern = "yyyy-MM-dd") @RequestParam("date") LocalDate endDate) {
        EndedTicketsResponse performanceTickets = findPerformanceService.findEndedPerformanceTickets(endDate);
        return ApiResponse.success(performanceTickets);
    }
}
