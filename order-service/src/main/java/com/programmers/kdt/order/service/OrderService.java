package com.programmers.kdt.order.service;

import com.programmers.kdt.order.dto.*;
import com.programmers.kdt.settlement.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    // 주문 요청
    CreateOrderResponse createOrder(CreateOrderRequest request);

    // 주문 완료
    void completeOrder(Long orderId);

    // 주문 만료
    void expireOrders();

    // 주문 만료 조회
    void startPayment(Long orderId);

    // 주문 취소
    CancelOrderResponse cancelCompletedOrder(Long orderId, CancelOrderRequest request);

    // 주문 내역 조회
    List<GetOrderHistoryResponse> getOrderHistory(Long userId);

    CancelOrderResponse cancelPendingOrder(Long orderId);

    List<OrderResponse> getOrders(List<Long> ticketIds);
}
