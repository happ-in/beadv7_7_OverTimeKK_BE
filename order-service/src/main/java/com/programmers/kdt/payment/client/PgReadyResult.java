package com.programmers.kdt.payment.client;

public record PgReadyResult(
        String transactionKey,
        String redirectionUrl
) {
}
