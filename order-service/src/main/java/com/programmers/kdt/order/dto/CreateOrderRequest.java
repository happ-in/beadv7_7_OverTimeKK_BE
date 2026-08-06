package com.programmers.kdt.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateOrderRequest(
        @NotNull
        Long ticketId,

        @NotNull
        Long userId,

        @NotNull
        Long price,

        @NotBlank
        String holdKey

) {
}
