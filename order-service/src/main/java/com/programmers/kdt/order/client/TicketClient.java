package com.programmers.kdt.order.client;

import com.programmers.kdt.order.dto.OrderTicketRequest;
import com.programmers.kdt.order.dto.TicketCancelRequest;
import com.programmers.kdt.order.dto.TicketReleaseRequest;
import com.programmers.kdt.order.dto.TicketReserveRequest;
import com.programmers.kdt.order.dto.ValidateTicketRequest;

import java.util.List;

public interface TicketClient {

    void validateTicket(ValidateTicketRequest ticketRequest);
    void reserveTicket(TicketReserveRequest ticketReserveRequest);
    void releaseSeat(TicketReleaseRequest request);
    void cancelTicket(TicketCancelRequest request);
    List<TicketInfo> getTickets(OrderTicketRequest request);
}
