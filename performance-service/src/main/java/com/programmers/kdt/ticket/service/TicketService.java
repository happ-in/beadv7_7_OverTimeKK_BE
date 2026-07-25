package com.programmers.kdt.ticket.service;

import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;

public interface TicketService {
    CreateStandbyResponse issueStandby(Long userId, Long sessionNum, String zone);

    SessionStartDateResponse getSessionStartDate(Long ticketId);
}
