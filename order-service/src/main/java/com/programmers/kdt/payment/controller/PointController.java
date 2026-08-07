package com.programmers.kdt.payment.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.payment.dto.GetPointBalanceResponse;
import com.programmers.kdt.payment.dto.GetPointHistoryResponse;
import com.programmers.kdt.payment.service.PointService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/balance")
    public ApiResponse<GetPointBalanceResponse> getPointBalance(
            @RequestParam Long userId
    ) {
        return ApiResponse.success(pointService.getBalance(userId));
    }

    @GetMapping
    public ApiResponse<Page<GetPointHistoryResponse>> getPointHistory(
            @RequestParam Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(pointService.getPointLogHistory(userId, pageable));
    }
}
