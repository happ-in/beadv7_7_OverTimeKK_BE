package com.programmers.kdt.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record OrderTicketRequest(
        @NotNull(message = "구매자 ID 입력은 필수입니다.")
        Long userId
) {
}
