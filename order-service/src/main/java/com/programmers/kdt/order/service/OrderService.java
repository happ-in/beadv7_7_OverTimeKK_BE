package com.programmers.kdt.order.service;

import com.programmers.kdt.order.dto.CreateOrderRequest;
import com.programmers.kdt.order.dto.CreateOrderResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {


    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        return null;
    }

    public void cancelOrder(Long orderId) {
        // TODO
    }
}