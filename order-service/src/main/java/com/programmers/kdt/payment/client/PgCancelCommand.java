package com.programmers.kdt.payment.client;

public record PgCancelCommand(
        String transactionKey,
        Long cancelAmount,
        String reason
) {
}
