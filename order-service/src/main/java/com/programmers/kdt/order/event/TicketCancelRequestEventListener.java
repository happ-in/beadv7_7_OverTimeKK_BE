package com.programmers.kdt.order.event;

import com.programmers.kdt.order.client.TicketClient;
import com.programmers.kdt.order.dto.TicketCancelRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCancelRequestEventListener {

    private final TicketClient ticketClient;

    @TransactionalEventListener
    public void handle(TicketCancelRequestEvent event) {
        try {
            ticketClient.cancelTicket(new TicketCancelRequest(event.ticketId(), event.userId()));
        } catch (RestClientException e) {
            log.error(
                    "결제 완료 티켓 취소 요청 실패 : orderId={}, ticketId={}",
                    event.orderId(),
                    event.ticketId()
            );
        }
    }
}
