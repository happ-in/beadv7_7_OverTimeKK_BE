package com.programmers.kdt.ticket.event;

import com.programmers.kdt.standby.event.StandbyTicketEvent;
import com.programmers.kdt.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TicketEventListener {

    private final TicketService ticketService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStandbyTicket(StandbyTicketEvent event) {
        ticketService.standbyTicket(event);
    }
}