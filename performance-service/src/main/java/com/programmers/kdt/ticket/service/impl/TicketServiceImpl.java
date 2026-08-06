package com.programmers.kdt.ticket.service.impl;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.entity.PerformanceSeatPrice;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceSeatPriceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.standby.event.StandbyTicketEvent;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.OrderTicketResponse;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.dto.TicketZoneRequest;
import com.programmers.kdt.ticket.dto.TicketZoneResponse;
import com.programmers.kdt.ticket.dto.TicketZonesResponse;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import com.programmers.kdt.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final PerformanceSessionRepository sessionRepository;
    private final PerformanceSeatPriceRepository performanceSeatPriceRepository;

    @Override
    public CreateStandbyResponse issueStandby(Long userId, Long sessionNum, String zone) {
        // TODO: (sessionNum, zone) 단위로 순번 채번
        return null;
    }

    @Override
    public SessionStartDateResponse getSessionStartDate(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(TicketErrorCode.TICKET_NOT_FOUND, ticketId));

        PerformanceSession session = sessionRepository.findById(new PerformanceSessionId(ticket.getSessionNum(), ticket.getPerformanceId()))
                .orElseThrow(() -> new BusinessException(PerformanceErrorCode.PERFORMANCE_SESSION_NOT_FOUND));

        LocalDate sessionStartDate = session.getPerformanceStartAt().toLocalDate();
        return new SessionStartDateResponse(sessionStartDate);
    }

    @Override
    @Transactional
    public void standbyTicket(StandbyTicketEvent event) {
        Ticket ticket = getTicket(event.ticketId());
        ticket.standbyTicket(event.standbyUserId(), event.standbyExpiredAt());
    }

    @Override
    public List<OrderTicketResponse> findOrderedTicketInfo(Long userId) {
        // TODO : 페이징처리 필요
        return ticketRepository.findTicketsByBuyUserId(userId);
    }

    @Override
    public TicketZonesResponse getTicketZone(TicketZoneRequest request) {
        List<TicketZoneResponse> response = ticketRepository.findByZoneAndPerformance(request.performanceId(), request.sessionNum(), request.zone());
        PerformanceSeatPrice seatPrice = performanceSeatPriceRepository.findByPerformance_PerformanceIdAndZone(request.performanceId(), request.zone());
        return new TicketZonesResponse(seatPrice.getZone(), seatPrice.getPrice(), response);
    }

    private @NonNull Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(TicketErrorCode.TICKET_NOT_FOUND));
    }
}