package com.programmers.kdt.order.dto;

import com.programmers.kdt.order.entity.Order;
import jakarta.validation.constraints.NotNull;

public record CancelOrderResponse(
        Long orderId,
        String orderStatus
) {
    public static CancelOrderResponse from(Order order){
        return new CancelOrderResponse(
                order.getOrderId(),
                order.getOrderStatus().name()
        );
    }
}
