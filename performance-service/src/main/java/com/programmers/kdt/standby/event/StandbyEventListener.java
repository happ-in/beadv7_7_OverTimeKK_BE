package com.programmers.kdt.standby.event;

import com.programmers.kdt.standby.service.StandbyService;
import com.programmers.kdt.ticket.event.TryMatchEvent;
import lombok.RequiredArgsConstructor;
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
    public void handle(TryMatchEvent event) {
        standbyService.tryMatch(event.ticketId());
    }
}
