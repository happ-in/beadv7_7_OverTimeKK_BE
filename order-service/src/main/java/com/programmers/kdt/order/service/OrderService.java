package com.programmers.kdt.order.service;

import com.programmers.kdt.order.dto.CreateOrderRequest;
import com.programmers.kdt.order.dto.CreateOrderResponse;
import com.programmers.kdt.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;

    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // TODO: userClient.existsUser(request.userId())로 회원 존재 확인 후 주문 생성
        return null;
    }

    public void cancelOrder(Long orderId) {
        // TODO
    }
}
