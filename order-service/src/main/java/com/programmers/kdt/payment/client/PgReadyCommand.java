package com.programmers.kdt.payment.client;

public record PgReadyCommand(
        Long orderId,
        Long amount
) {
}
