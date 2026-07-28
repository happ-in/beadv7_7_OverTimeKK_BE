package com.programmers.kdt.order.client;


import com.programmers.kdt.order.dto.TicketHoldRequest;
import com.programmers.kdt.order.dto.TicketHoldResult;
import com.programmers.kdt.order.dto.TicketReleaseRequest;

import java.util.List;

public interface TicketClient {

    TicketHoldResult holdSeat(TicketHoldRequest ticketRequest);
    void releaseSeat(TicketReleaseRequest request);
    List<TicketInfo> getTickets(Long userId);
}
