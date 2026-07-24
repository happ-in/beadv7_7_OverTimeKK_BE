package com.programmers.kdt.order.client;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MockTicketClient implements TicketClient {
    public TicketHoldResult holdSeat(Long ticketId, Long userId){
        return new TicketHoldResult(ticketId, 50_000L, LocalDateTime.now().plusMinutes(10));
    }

    public void releaseSeat(Long ticketId, Long userId){
        System.out.println("좌석 점유 해제 요청 성공: ticketId= " + ticketId
                            + ", userId= " + userId);
    }

    public TicketInfo getTicket(Long ticketId){
        return new TicketInfo(
                ticketId,
                "오페라의 유령",
                "R"
        );
    }
}
