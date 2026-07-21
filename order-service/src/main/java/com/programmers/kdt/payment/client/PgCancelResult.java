package com.programmers.kdt.payment.client;

import java.time.LocalDateTime;

public record PgCancelResult(
        boolean success,
        LocalDateTime canceledAt
) {
}
