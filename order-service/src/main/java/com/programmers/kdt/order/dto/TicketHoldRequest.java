package com.programmers.kdt.order.dto;

public record TicketHoldRequest(
        Long ticketId,
        Long userId,
        String orderType

) {
    public static TicketHoldRequest from(CreateOrderRequest request){
        return new TicketHoldRequest(request.ticketId(), request.userId(), request.orderType());
    }
}
