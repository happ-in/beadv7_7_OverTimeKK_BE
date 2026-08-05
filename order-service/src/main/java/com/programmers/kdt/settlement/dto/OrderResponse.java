package com.programmers.kdt.settlement.dto;

import com.programmers.kdt.order.entity.Order;

public record OrderResponse(
        Long orderId,
        Long ticketId
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getTicketId()
        );
    }
}
