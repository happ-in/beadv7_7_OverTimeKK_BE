package com.programmers.kdt.ticket.service;

import com.programmers.kdt.standby.event.StandbyCheckResponseEvent;
import com.programmers.kdt.ticket.dto.CancelTicketStatusRequest;
import com.programmers.kdt.ticket.dto.ReleaseTicketHoldRequest;
import com.programmers.kdt.ticket.dto.SessionZoneKey;

import java.util.Map;

public interface TicketReleaseService {
    void releaseHoldTicket(ReleaseTicketHoldRequest request);

    void releaseExpiredHoldTicket(Long ticketId, Map<SessionZoneKey, Boolean> zoneAvailabilityCache);

    void changeTicketStatusByStandby(StandbyCheckResponseEvent event);

    void cancelReservedTicket(CancelTicketStatusRequest request);
}