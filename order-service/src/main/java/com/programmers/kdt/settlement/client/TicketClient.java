package com.programmers.kdt.settlement.client;

import com.programmers.kdt.settlement.dto.GetTicketsRequest;
import com.programmers.kdt.settlement.dto.GetTicketsResponse;

import java.util.List;

public interface TicketClient {
    List<GetTicketsResponse> getTickets(List<GetTicketsRequest> sessions);
}
