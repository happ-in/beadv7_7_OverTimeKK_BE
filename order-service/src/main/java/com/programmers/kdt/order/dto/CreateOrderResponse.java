package com.programmers.kdt.order.dto;

import com.programmers.kdt.order.entity.Order;
import jakarta.validation.constraints.NotNull;

public record CreateOrderResponse(
        @NotNull
        Long orderId,

        @NotNull
        String orderStatus
) {
        public static CreateOrderResponse from(Order order){
                return new CreateOrderResponse(order.getOrderId(), order.getOrderStatus().name());
        }
}
