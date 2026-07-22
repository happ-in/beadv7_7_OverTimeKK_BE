package com.programmers.kdt.payment.client.pg;

public record PgApproveCommand(
        String transactionKey,
        Long amount
) {
}
