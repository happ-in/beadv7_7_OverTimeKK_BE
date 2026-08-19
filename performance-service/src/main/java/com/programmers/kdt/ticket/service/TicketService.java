package com.programmers.kdt.ticket.service;

import com.programmers.kdt.standby.event.StandbyTicketEvent;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.OrderTicketResponse;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.dto.TicketZoneRequest;
import com.programmers.kdt.ticket.dto.TicketZonesResponse;

import java.util.List;

public interface TicketService {
    CreateStandbyResponse issueStandby(Long userId, Long sessionNum, String zone);

    SessionStartDateResponse getSessionStartDate(Long ticketId);

    void standbyTicket(StandbyTicketEvent event);

    List<OrderTicketResponse> findOrderedTicketInfo(List<Long> ticketIds);

    TicketZonesResponse getTicketZone(TicketZoneRequest request);
}