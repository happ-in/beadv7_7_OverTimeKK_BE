package com.programmers.kdt.order.dto;

import com.programmers.kdt.order.client.TicketInfo;
import com.programmers.kdt.order.entity.Order;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record GetOrderHistoryResponse(
        Long orderId,
        String performanceName,
        LocalDateTime orderedAt,
        String zone,
        int quantity,
        Long totalAmount
) {
    public static GetOrderHistoryResponse from(Order order, TicketInfo ticketInfo){
        return new GetOrderHistoryResponse(
                order.getOrderId(),
                ticketInfo.performanceName(),
                order.getCreatedAt(),
                ticketInfo.zone(),
                order.getItems().size(),
                order.getTotalAmount()
        );
    }
}
