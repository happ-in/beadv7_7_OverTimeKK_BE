package com.programmers.kdt.order.event;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.client.TicketClient;
import com.programmers.kdt.order.dto.TicketCancelRequest;
import com.programmers.kdt.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketCancelRequestEventListener {

    private final TicketClient ticketClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TicketCancelRequestEvent event) {
        int maxAttempt = 3;
        long retryDelayMs = 200L;
        for(int attempt = 1; attempt<= maxAttempt; attempt++){
            try {
                ticketClient.cancelTicket(new TicketCancelRequest(event.ticketId(), event.userId()));
                return;
            } catch (BusinessException e) {
                if(!OrderErrorCode.TICKET_RELEASE_FAILED.equals(e.getErrorCode())){
                    log.error(
                            "티켓 취소 요청 실패(재시도 대상 아님) : orderId={}, ticketId={}, code={}",
                            event.orderId(), event.ticketId(), e.getErrorCode()
                    );
                    return;
                }

                log.warn(
                        "[티켓 취소 재시도] orderId={} ticketId={} attempt={}/{}",
                        event.orderId(), event.ticketId(), attempt, maxAttempt
                );
                if(attempt == maxAttempt){
                    log.error(
                            "결제 완료 티켓 취소 요청 실패 : orderId={}, ticketId={}",
                            event.orderId(), event.ticketId()
                    );
                    return;
                }

                try{
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
