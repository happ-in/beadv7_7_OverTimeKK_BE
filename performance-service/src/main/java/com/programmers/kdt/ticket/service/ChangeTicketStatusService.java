package com.programmers.kdt.ticket.service;

import com.programmers.kdt.standby.event.StandbyCheckResponseEvent;
import com.programmers.kdt.ticket.dto.CancelTicketStatusRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.ReleaseTicketHoldRequest;
import com.programmers.kdt.ticket.dto.ReservedTicketRequest;
import com.programmers.kdt.ticket.dto.SessionZoneKey;

import java.util.List;
import java.util.Map;

public interface ChangeTicketStatusService {
    CheckTicketHoldAvailableResponse checkTicketHoldStatus(CheckTicketHoldAvailableRequest request);

    void releaseHoldTicket(ReleaseTicketHoldRequest request);

    List<Long> findExpiredHoldTicketIds();

    void releaseExpiredHoldTicket(Long ticketId, Map<SessionZoneKey, Boolean> zoneAvailabilityCache);

    void changeTicketStatusByStandby(StandbyCheckResponseEvent event);

    void cancelReservedTicket(CancelTicketStatusRequest request);

    void reservedTicket(ReservedTicketRequest request);
}