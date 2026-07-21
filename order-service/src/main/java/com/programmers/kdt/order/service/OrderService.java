package com.programmers.kdt.order.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.exception.OrderErrorCode;
import com.programmers.kdt.order.dto.CreateOrderRequest;
import com.programmers.kdt.order.dto.CreateOrderResponse;
import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    OrderRepository orderRepository;

    // 주문 생성
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // 1. 사용자 확인

        // 2. 좌석 점유 확인 및 요청
        // Ticket ticket = ticketService.holdSeat(request.ticketId(), request.userId()); // Ticket 반환

        // 3. 주문 생성
        // Order order = Order.create(request.userId(), );
        // 4. 주문 저장
        // orderRepository.save(order);

        // 4. 결제 생성 요청
        //paymentService.create();
        return null;
    }
    // 주문 완료
    public void completeOrder(Long orderId){
        Order order = findOrder(orderId);
        order.complete();
    }

    // 주문 만료
    public void expiredOrder(Long orderId){
        Order order = findOrder(orderId);
        order.expire();
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId) {
        // 1. 주문  조회
        Order order = findOrder(orderId);

        // 2. 결제 취소
        // paymentService.cancel(orderId);
        // 3. 주문 취소
        order.cancel();

        // 4. 5분 뒤, 좌석 점유 해제 예약
        // ticketServcie.scheduleRelease(ticketId); -> userId도 함께 보내줘야 되는지
    }

    private Order findOrder(Long orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(()->
                        new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}