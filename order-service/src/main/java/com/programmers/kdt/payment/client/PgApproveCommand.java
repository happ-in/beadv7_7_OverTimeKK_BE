package com.programmers.kdt.payment.client;

public record PgApproveCommand(
        String transactionKey,
        Long amount
) {
}
