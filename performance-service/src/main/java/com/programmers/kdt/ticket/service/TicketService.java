package com.programmers.kdt.ticket.service;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.dto.StandbyTicketRequest;

public interface TicketService {
    CreateStandbyResponse issueStandby(Long userId, Long sessionNum, String zone);

    SessionStartDateResponse getSessionStartDate(Long ticketId);

    /* 좌석점유 상태 확인, 점유가능한 상태면 hold하고 만료시간 return */
    CheckTicketHoldAvailableResponse checkTicketHoldStatus(CheckTicketHoldAvailableRequest request);

    void releaseHoldTicket(Long ticketId);

    void standbyTicket(StandbyTicketRequest request);
}
