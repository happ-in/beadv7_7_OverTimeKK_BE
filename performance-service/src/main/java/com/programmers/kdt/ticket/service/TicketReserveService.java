package com.programmers.kdt.ticket.service;

import com.programmers.kdt.ticket.dto.ReservedTicketRequest;

public interface TicketReserveService {
    void reservedTicket(ReservedTicketRequest request);
}