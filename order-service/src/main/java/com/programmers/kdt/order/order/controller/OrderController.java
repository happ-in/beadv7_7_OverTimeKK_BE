package com.programmers.kdt.order.order.controller;

import com.programmers.kdt.order.order.dto.CreateOrderRequest;
import com.programmers.kdt.order.order.dto.CreateOrderResponse;
import com.programmers.kdt.order.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @PostMapping("/{orderId}/cancel")
    public void cancel(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
    }
}
