package com.programmers.kdt.order.event;

import com.programmers.kdt.order.service.TicketCancelJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TicketCancelRequestEventListener {

    private final TicketCancelJobService ticketCancelJobService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TicketCancelRequestEvent event) {
        ticketCancelJobService.process(event.orderId(), event.ticketId(), event.userId());
    }
}
