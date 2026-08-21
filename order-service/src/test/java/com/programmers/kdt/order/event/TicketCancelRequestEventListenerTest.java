package com.programmers.kdt.order.event;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.client.TicketClient;
import com.programmers.kdt.order.exception.OrderErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketCancelRequestEventListenerTest {

    @Mock
    private TicketClient ticketClient;

    private TicketCancelRequestEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new TicketCancelRequestEventListener(ticketClient);
    }

    @Test
    @DisplayName("첫 호출이 성공하면 재시도 없이 cancelTicket을 1번만 호출한다")
    void firstAttemptSucceeds() {
        TicketCancelRequestEvent event = new TicketCancelRequestEvent(10L, 900001L, 1L);

        listener.handle(event);

        verify(ticketClient, times(1)).cancelTicket(any());
    }

    @Test
    @DisplayName("1회 실패 후 성공하면 cancelTicket을 2번 호출하고 종료한다")
    void succeedsOnSecondAttempt() {
        doThrow(ticketReleaseFailed())
                .doNothing()
                .when(ticketClient).cancelTicket(any());

        listener.handle(new TicketCancelRequestEvent(10L, 900001L, 1L));

        verify(ticketClient, times(2)).cancelTicket(any());
    }

    @Test
    @DisplayName("2회 실패 후 성공하면 cancelTicket을 3번 호출하고 종료한다")
    void succeedsOnThirdAttempt() {
        doThrow(ticketReleaseFailed())
                .doThrow(ticketReleaseFailed())
                .doNothing()
                .when(ticketClient).cancelTicket(any());

        listener.handle(new TicketCancelRequestEvent(10L, 900001L, 1L));

        verify(ticketClient, times(3)).cancelTicket(any());
    }

    @Test
    @DisplayName("3회 모두 실패해도 예외를 밖으로 던지지 않고 cancelTicket은 3번만 호출한다")
    void allAttemptsFailWithoutPropagating() {
        doThrow(ticketReleaseFailed())
                .when(ticketClient).cancelTicket(any());

        assertDoesNotThrow(() ->
                listener.handle(new TicketCancelRequestEvent(10L, 900001L, 1L))
        );

        verify(ticketClient, times(3)).cancelTicket(any());
    }

    @Test
    @DisplayName("TICKET_NOT_FOUND는 재시도하지 않고 cancelTicket을 1번만 호출한다")
    void ticketNotFoundIsNotRetried() {
        doThrow(new BusinessException(OrderErrorCode.TICKET_NOT_FOUND))
                .when(ticketClient).cancelTicket(any());

        assertDoesNotThrow(() ->
                listener.handle(new TicketCancelRequestEvent(10L, 900001L, 1L))
        );

        verify(ticketClient, times(1)).cancelTicket(any());
    }

    private static BusinessException ticketReleaseFailed() {
        return new BusinessException(OrderErrorCode.TICKET_RELEASE_FAILED);
    }
}
