package com.programmers.kdt.payment.client;

public interface PgClient {
    PgReadyResult ready(PgReadyCommand command);
    PgApproveResult approve(PgApproveCommand command);
    PgCancelResult cancel(PgCancelCommand command);
}
