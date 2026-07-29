package com.programmers.kdt.order.client;


import com.programmers.kdt.order.dto.*;

import java.util.List;

public interface TicketClient {

    TicketHoldResult holdSeat(TicketHoldRequest ticketRequest);
    void reserveTicket(TicketReserveRequest ticketReserveRequest);
    void releaseSeat(TicketReleaseRequest request);
    void cancelTicket(TicketCancelRequest request);
    List<TicketInfo> getTickets(Long userId);
}
