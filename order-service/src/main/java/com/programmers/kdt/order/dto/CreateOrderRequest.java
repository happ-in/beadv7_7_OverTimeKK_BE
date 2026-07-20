package com.programmers.kdt.order.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull
        Long userId, // 로그인 토큰에서 서버가 꺼냄

        @NotNull
        Long ticketId
) {
}
