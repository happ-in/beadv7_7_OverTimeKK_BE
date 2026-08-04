package com.programmers.kdt.ticket.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.repository.PerformanceSeatPriceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.event.StandbyCheckRequestEvent;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PerformanceSessionRepository sessionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PerformanceSeatPriceRepository performanceSeatPriceRepository;

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(ticketRepository, sessionRepository, eventPublisher, performanceSeatPriceRepository);
    }

    private static Ticket holdTicket(Long ticketId, Long userId, LocalDateTime holdExpiredAt, String holdKey) {
        Ticket ticket = Ticket.create(1L, 1L, "A", "1", String.valueOf(ticketId), 10000L);
        ReflectionTestUtils.setField(ticket, "ticketId", ticketId);
        ticket.holdTicket(userId, holdExpiredAt, holdKey);
        return ticket;
    }

    @Nested
    @DisplayName("점유 만료 티켓 자동 해제")
    class ReleaseExpiredHoldTickets {

        @Test
        @DisplayName("점유시간이 지난 HOLD 티켓이 없으면 아무 것도 하지 않는다.")
        void noExpiredTickets_doesNothing() {
            when(ticketRepository.findByTicketStatusAndHoldExpiredAtBefore(eq(TicketStatus.HOLD), any()))
                    .thenReturn(List.of());

            ticketService.releaseExpiredHoldTickets();

            verify(ticketRepository, never()).findById(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("점유시간이 지난 티켓은 같은 구역에 잔여석이 있으면 AVAILABLE로 자동 해제된다.")
        void expiredHoldTicket_releasedToAvailable() {
            Ticket ticket = holdTicket(1L, 100L, LocalDateTime.now().minusMinutes(1), "hold-key");

            when(ticketRepository.findByTicketStatusAndHoldExpiredAtBefore(eq(TicketStatus.HOLD), any()))
                    .thenReturn(List.of(ticket));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(any(), any(), eq(TicketStatus.AVAILABLE), anyString()))
                    .thenReturn(true);

            ticketService.releaseExpiredHoldTickets();

            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.AVAILABLE);
            assertThat(ticket.getBuyUserId()).isNull();
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("점유시간이 지난 티켓은 같은 구역이 매진 상태면 대기 확인 이벤트를 발행한다.")
        void expiredHoldTicket_zoneSoldOut_publishesStandbyCheckEvent() {
            Ticket ticket = holdTicket(1L, 100L, LocalDateTime.now().minusMinutes(1), "hold-key");

            when(ticketRepository.findByTicketStatusAndHoldExpiredAtBefore(eq(TicketStatus.HOLD), any()))
                    .thenReturn(List.of(ticket));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(any(), any(), eq(TicketStatus.AVAILABLE), anyString()))
                    .thenReturn(false);

            ticketService.releaseExpiredHoldTickets();

            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.HOLD);
            verify(eventPublisher).publishEvent(any(StandbyCheckRequestEvent.class));
        }

        @Test
        @DisplayName("여러 건이 만료됐으면 모두 해제 처리된다.")
        void multipleExpiredTickets_allReleased() {
            Ticket ticket1 = holdTicket(1L, 100L, LocalDateTime.now().minusMinutes(1), "key1");
            Ticket ticket2 = holdTicket(2L, 200L, LocalDateTime.now().minusMinutes(2), "key2");

            when(ticketRepository.findByTicketStatusAndHoldExpiredAtBefore(eq(TicketStatus.HOLD), any()))
                    .thenReturn(List.of(ticket1, ticket2));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket1));
            when(ticketRepository.findById(2L)).thenReturn(Optional.of(ticket2));
            when(ticketRepository.existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(any(), any(), eq(TicketStatus.AVAILABLE), anyString()))
                    .thenReturn(true);

            ticketService.releaseExpiredHoldTickets();

            assertThat(ticket1.getTicketStatus()).isEqualTo(TicketStatus.AVAILABLE);
            assertThat(ticket2.getTicketStatus()).isEqualTo(TicketStatus.AVAILABLE);
            verify(ticketRepository, times(2)).existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(any(), any(), eq(TicketStatus.AVAILABLE), anyString());
        }

        @Test
        @DisplayName("만료 조회 시점과 처리 시점 사이에 이미 다른 경로로 해제된 티켓은 예외를 던지고, 뒤에 남은 티켓은 처리되지 않는다.")
        void alreadyReleasedTicket_throwsAndStopsRemainingProcessing() {
            Ticket alreadyReleased = holdTicket(1L, 100L, LocalDateTime.now().minusMinutes(1), "key1");
            alreadyReleased.releaseToAvailable();
            Ticket stillHeld = holdTicket(2L, 200L, LocalDateTime.now().minusMinutes(2), "key2");

            when(ticketRepository.findByTicketStatusAndHoldExpiredAtBefore(eq(TicketStatus.HOLD), any()))
                    .thenReturn(List.of(alreadyReleased, stillHeld));
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(alreadyReleased));

            assertThatThrownBy(() -> ticketService.releaseExpiredHoldTickets())
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(TicketErrorCode.TICKET_IS_NOT_HELD);

            verify(ticketRepository, never()).findById(2L);
        }
    }
}