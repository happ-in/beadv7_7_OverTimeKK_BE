package com.programmers.kdt.ticket.service;

import com.programmers.kdt.standby.event.StandbyTicketEvent;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;

public interface TicketService {
    CreateStandbyResponse issueStandby(Long userId, Long sessionNum, String zone);

    SessionStartDateResponse getSessionStartDate(Long ticketId);

    CheckTicketHoldAvailableResponse checkTicketHoldStatus(CheckTicketHoldAvailableRequest request);

    void releaseHoldTicket(Long ticketId);

    void standbyTicket(StandbyTicketEvent event);
}
