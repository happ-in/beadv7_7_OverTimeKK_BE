package com.programmers.kdt.standby.event;

import com.programmers.kdt.standby.service.StandbyService;
import com.programmers.kdt.ticket.event.StandbyCheckRequestEvent;
import com.programmers.kdt.ticket.event.StandbyTicketReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StandbyEventListener {

    private final StandbyService standbyService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCheckStandbyHandler(StandbyCheckRequestEvent event) {
        standbyService.StandbyCheck(event);
    }

    @EventListener void onTicketReservedHandler(StandbyTicketReservedEvent event) {
        standbyService.reservedStandby(event.ticketId());
    }
}
