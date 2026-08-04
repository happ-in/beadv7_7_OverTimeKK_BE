package com.programmers.kdt.order.client;


import com.programmers.kdt.order.dto.*;

import java.util.List;

public interface TicketClient {

    void validateTicket(ValidateTicketRequest ticketRequest);
    void reserveTicket(TicketReserveRequest ticketReserveRequest);
    void releaseSeat(TicketReleaseRequest request);
    void cancelTicket(TicketCancelRequest request);
    List<TicketInfo> getTickets(OrderTicketRequest request);
}
