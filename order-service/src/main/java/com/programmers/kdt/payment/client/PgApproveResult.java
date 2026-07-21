package com.programmers.kdt.payment.client;

import java.time.LocalDateTime;

public record PgApproveResult(
        boolean success,
        LocalDateTime approvedAt
) {
}
