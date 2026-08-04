package com.programmers.kdt.order.dto;

import java.time.LocalDateTime;

public record ValidateTicketRequest(
        Long ticketId,
        Long userId,
        Long price,
        LocalDateTime expiredAt,
        String holdKey
) {
    public static ValidateTicketRequest from(CreateOrderRequest request){
        return new ValidateTicketRequest(request.ticketId(), request.userId(), request.price(), request.expiredAt(), request.holdKey());
    }
}