package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.PerformanceSeatPriceRequest;
import com.programmers.kdt.performance.dto.PerformanceV2Request;
import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.dto.RegisterPerformanceSessionRequest;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSeatPrice;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.performance.repository.PerformanceSeatPriceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceV2ServiceTest {

    @Mock private PerformanceSeatPriceRepository seatPriceRepository;
    @Mock private PerformanceRepository performanceRepository;
    @Mock private PerformanceSessionRepository performanceSessionRepository;
    @Mock private TicketRepository ticketRepository;

    @InjectMocks private PerformanceV2Service performanceService;

    @Test
    @DisplayName("판매자가 새 공연 글을 등록한다.")
    void registerNewPerformance() {
        // given
        RegisterPerformanceRequest request = mock(RegisterPerformanceRequest.class);

        Performance performance = Performance.createPerformance(
                "여름왕국"
                , "더워요.,,"
                , 150L
                , LocalDate.of(2026, 8, 1)
                , LocalDate.of(2026, 8, 30)
                , LocalDateTime.of(2026, 7, 31, 10, 0)
                , 1L
                , 2L
        );
        ReflectionTestUtils.setField(performance, "performanceId", 5L);
        given(request.toPerformance()).willReturn(performance);

        given(performanceRepository.save(any(Performance.class))).willReturn(performance);

        PerformanceSession session = PerformanceSession.create(
                new PerformanceSessionId(1L, 5L),
                performance,
                "김이박최",
                LocalDateTime.now()
        );
        List<PerformanceSession> sessions = List.of(session);
        given(request.toSessions(performance)).willReturn(sessions);
        given(performanceSessionRepository.saveAll(anyList())).willReturn(sessions);

        PerformanceSeatPrice seatPrice = PerformanceSeatPrice.createInitial(performance, "VIP", 150000L);
        ReflectionTestUtils.setField(seatPrice, "id", 1L);
        List<PerformanceSeatPrice> seatPrices = List.of(seatPrice);
        given(request.toSeatPrices(performance)).willReturn(seatPrices);
        given(seatPriceRepository.saveAll(anyList())).willReturn(seatPrices);

        given(ticketRepository.issueTicket(5L, TicketStatus.AVAILABLE)).willReturn(1);

        // when
        performanceService.registerPerformanceInformation(request);

        // then
        assertThat(performance.getPerformanceId()).isEqualTo(5L);

        verify(performanceRepository).save(performance);
        verify(performanceSessionRepository).saveAll(sessions);
        verify(seatPriceRepository).saveAll(seatPrices);

    }

    @Test
    @DisplayName("판매자가 새 공연을 등록한다.")
    void registerPerformanceInformation() {

        RegisterPerformanceSessionRequest sessionRequest1 = new RegisterPerformanceSessionRequest(
                1L,
                "도라에몽",
                LocalDateTime.of(2026, 8, 15, 14, 0, 0)
        );

        RegisterPerformanceSessionRequest sessionRequest2 = new RegisterPerformanceSessionRequest(
                2L,
                "도라미",
                LocalDateTime.of(2026, 8, 15, 20, 0, 0)
        );


        PerformanceSeatPriceRequest seatPriceRequest1 = new PerformanceSeatPriceRequest("VIP", 150000L);
        PerformanceSeatPriceRequest seatPriceRequest2 = new PerformanceSeatPriceRequest("R", 120000L);
        PerformanceSeatPriceRequest seatPriceRequest3 = new PerformanceSeatPriceRequest("S", 80000L);
        PerformanceSeatPriceRequest seatPriceRequest4 = new PerformanceSeatPriceRequest("A", 50000L);


        PerformanceV2Request performanceRequest = new PerformanceV2Request(
                "집인데 집에 가고 싶다.",
                "KDT 백엔드 단기심화 7기",
                150L,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                LocalDateTime.of(2026, 8, 1, 10, 0, 0),
                1L
        );

        RegisterPerformanceRequest request = new RegisterPerformanceRequest(
                1L
                , performanceRequest
                , List.of(sessionRequest1, sessionRequest2)
                , List.of(seatPriceRequest1, seatPriceRequest2, seatPriceRequest3, seatPriceRequest4)
        );

        Performance savedPerformance = request.toPerformance();
        ReflectionTestUtils.setField(savedPerformance, "performanceId", 1L);
        when(performanceRepository.save(any(Performance.class))).thenReturn(savedPerformance);
        when(ticketRepository.issueTicket(1L, TicketStatus.AVAILABLE)).thenReturn(5);

        // when
        performanceService.registerPerformanceInformation(request);

        // then
        // TODO : 값 빼서 작성하는거 필요.
//        ArgumentCaptor<List<PerformanceSession>> captorSessions = ArgumentCaptor.forClass(List.class);
//        verify(performanceSessionRepository).saveAll(captorSessions.capture());
//        List<List<PerformanceSession>> sessionsCaptor = captorSessions.getAllValues();

        verify(performanceRepository).save(any(Performance.class));
        verify(performanceSessionRepository).saveAll(anyList());
        verify(seatPriceRepository).saveAll(anyList());
    }
}