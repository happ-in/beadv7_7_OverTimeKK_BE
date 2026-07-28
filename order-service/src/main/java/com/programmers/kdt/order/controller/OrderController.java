package com.programmers.kdt.order.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.order.dto.*;
import com.programmers.kdt.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 요청
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 결제 전 주문 취소
    @PostMapping("/{orderId}/cancel-pending")
    public ApiResponse<CancelOrderResponse> cancelPendingOrder(
            @PathVariable Long orderId
    ) {
        CancelOrderResponse response =
                orderService.cancelPendingOrder(orderId);

        return ApiResponse.success(response);
    }

    // 결제 완료 후 주문 취소
    @PostMapping("/{orderId}/cancel-completed")
    public ApiResponse<CancelOrderResponse> cancelCompleteOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        CancelOrderResponse response = orderService.cancelCompletedOrder(orderId, request);
        return ApiResponse.success(response);
    }

    // 주문 내역 조회
    @GetMapping
    public ApiResponse<List<GetOrderHistoryResponse>> getOrderHistory(@RequestParam Long userId){
         List<GetOrderHistoryResponse> response = orderService.getOrderHistory(userId);
         return ApiResponse.success(response);
    }
}
