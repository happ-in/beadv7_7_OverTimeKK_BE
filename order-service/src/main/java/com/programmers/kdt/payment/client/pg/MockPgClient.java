package com.programmers.kdt.payment.client.pg;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

// 실제 PG사가 아닌 모킹으로 우선 구현(토스페이로 교체 예정)

@Profile("!toss")
@Component
public class MockPgClient implements PgClient {

    @Override
    public PgReadyResult ready(PgReadyCommand command) {
        String transactionKey = "Mock-" + UUID.randomUUID();
        String orderId = PgOrderIdFormatter.format(command.orderId());
        return new PgReadyResult(transactionKey, orderId, "https://mock-pg.local/pay/" + transactionKey);
    }

    @Override
    public PgApproveResult approve(PgApproveCommand command) {
        return new PgApproveResult(true, LocalDateTime.now());
    }

    @Override
    public PgCancelResult cancel(PgCancelCommand command) {
        return new PgCancelResult(true, LocalDateTime.now());
    }
}
