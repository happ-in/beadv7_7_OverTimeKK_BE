package com.programmers.kdt.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record CheckTicketHoldAvailableRequest(
        @NotNull(message  = "티켓 정보를 입력해주세요.")
        Long ticketId,

        @NotNull(message = "주문자 정보를 입력해주세요.")
        Long userId
) {
}
