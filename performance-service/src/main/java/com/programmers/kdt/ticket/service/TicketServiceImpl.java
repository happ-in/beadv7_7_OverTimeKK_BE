package com.programmers.kdt.ticket.service;

import com.programmers.kdt.common.TimeLimits;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.util.TicketKeyGenerator;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.standby.event.StandbyCheckResponseEvent;
import com.programmers.kdt.standby.event.StandbyTicketEvent;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.ReleaseTicketHoldRequest;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.event.StandbyCheckRequestEvent;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final PerformanceSessionRepository sessionRepository;

    private final ApplicationEventPublisher eventPublisher;

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
    public CheckTicketHoldAvailableResponse checkTicketHoldStatus(CheckTicketHoldAvailableRequest request) {
        Long userId = request.userId();
        Long ticketId = request.ticketId();

        Ticket ticket = getTicket(ticketId);

        // TODO : request에 orderType 분기 처리 필요.

        if (!isStandbyTicket(ticket, userId) && !isPossibleToHold(ticket)) {
            throw new BusinessException(TicketErrorCode.IMPOSSIBLE_HOLD_TICKET);
        }

        LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(TimeLimits.orderHoldTicket5Min);
        String holdKey = TicketKeyGenerator.generate();

        ticket.holdTicket(userId, holdExpiredAt, holdKey);
        return new CheckTicketHoldAvailableResponse(ticket.getTicketId(), ticket.getPrice(), holdExpiredAt, holdKey);
    }

    @Override
    @Transactional
    public void releaseHoldTicket(ReleaseTicketHoldRequest request) {
        Ticket ticket = getTicket(request.ticketId());

        if (!request.holdKey().equals(ticket.getHoldKey())) {
            throw new BusinessException(TicketErrorCode.HOLD_KEY_DISCREPANCY);
        }

        boolean existsAvailableInZone = ticketRepository.existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(ticket.getPerformanceId(), ticket.getSessionNum(), TicketStatus.AVAILABLE, ticket.getZone());
        log.info("{} 공연 {}회차 {} 구역 매진이 아닌가? {}", ticket.getPerformanceId(), ticket.getSessionNum(), ticket.getZone(), existsAvailableInZone);

        if (existsAvailableInZone) {
            ticket.releaseToAvailable();
        } else {
            eventPublisher.publishEvent(new StandbyCheckRequestEvent(ticket.getTicketId(), ticket.getSessionNum(), ticket.getZone(), ticket.getTicketId()));
        }
    }

    private @NonNull Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(TicketErrorCode.TICKET_NOT_FOUND));
    }

    @Override
    @Transactional
    public void standbyTicket(StandbyTicketEvent event) {
        Ticket ticket = getTicket(event.ticketId());
        ticket.standbyTicket(event.standbyUserId(), event.standbyExpiredAt());
    }

    @Override
    @Transactional
    public void changeTicketStatusByStandby(StandbyCheckResponseEvent event) {
        Ticket ticket = getTicket(event.ticketId());

        if (event.existsStandby()) {
            ticket.releaseToStandby();
        } else {
            ticket.releaseToAvailable();
        }
    }

    private boolean isStandbyTicket(Ticket ticket, Long userId) {
        return ticket.getTicketStatus() == TicketStatus.CANCELED &&
                userId.equals(ticket.getStandbyUserId()) &&
                LocalDateTime.now().isAfter(ticket.getStandbyExpiredAt())
        ;
    }

    private boolean isPossibleToHold(Ticket ticket) {
        return ticket.getTicketStatus() == TicketStatus.AVAILABLE;
    }
}
