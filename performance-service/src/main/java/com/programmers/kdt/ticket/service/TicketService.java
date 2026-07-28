package com.programmers.kdt.ticket.service;

import com.programmers.kdt.standby.event.StandbyCheckResponseEvent;
import com.programmers.kdt.standby.event.StandbyTicketEvent;
import com.programmers.kdt.ticket.dto.CancelTicketStatusRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.OrderTicketResponse;
import com.programmers.kdt.ticket.dto.ReleaseTicketHoldRequest;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.dto.TicketZoneRequest;
import com.programmers.kdt.ticket.dto.TicketZonesResponse;

import java.util.List;

public interface TicketService {
    CreateStandbyResponse issueStandby(Long userId, Long sessionNum, String zone);

    SessionStartDateResponse getSessionStartDate(Long ticketId);

    CheckTicketHoldAvailableResponse checkTicketHoldStatus(CheckTicketHoldAvailableRequest request);

    void releaseHoldTicket(ReleaseTicketHoldRequest request);

    void standbyTicket(StandbyTicketEvent event);

    void changeTicketStatusByStandby(StandbyCheckResponseEvent event);

    void cancelReservedTicket(CancelTicketStatusRequest request);

    List<OrderTicketResponse> findOrderedTicketInfo(Long userId);

    TicketZonesResponse getTicketZone(TicketZoneRequest request);
}
