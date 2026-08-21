package com.programmers.kdt.ticket.event;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketIssueEventListener {

    private final TicketRepository ticketRepository;

    @EventListener
    public void handleTicketIssue(TicketIssueEvent event) {
        int issueTicketCount = ticketRepository.issueTicket(event.performanceId(), event.ticketStatus());
        if (issueTicketCount <= 0) {
            throw new BusinessException(TicketErrorCode.TICKET_ISSUE_FAILED);
        }
    }
}