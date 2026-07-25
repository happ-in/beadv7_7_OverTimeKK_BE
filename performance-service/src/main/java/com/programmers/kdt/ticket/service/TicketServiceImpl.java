package com.programmers.kdt.ticket.service;

import com.programmers.kdt.common.TimeLimits;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableRequest;
import com.programmers.kdt.ticket.dto.CheckTicketHoldAvailableResponse;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.dto.StandbyTicketRequest;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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


    public void holdSeat(Long ticketId, Long userId) {
        // TODO
    }

    public void releaseHold(Long ticketId) {
        // TODO
    }

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

        if (!isStandbyTicket(ticket, userId) && !isPossibleToHold(ticket)) {
            throw new BusinessException(TicketErrorCode.IMPOSSIBLE_HOLD_TICKET);
        }

        LocalDateTime holdExpiredAt = LocalDateTime.now().plusMinutes(TimeLimits.orderHoldTicket5Min);
        ticket.holdTicket(userId, holdExpiredAt);
        return new CheckTicketHoldAvailableResponse(ticket.getTicketId(), ticket.getPrice(), holdExpiredAt);
    }

    @Override
    @Transactional
    public void releaseHoldTicket(Long ticketId) {
        Ticket ticket = getTicket(ticketId);

        if (LocalDateTime.now().isBefore(ticket.getHoldExpiredAt())) {
            throw new BusinessException(TicketErrorCode.HOLD_TICKET_NOT_EXPIRED);
        }

        boolean isAvailableInZone = ticketRepository.existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(ticket.getPerformanceId(), ticket.getSessionNum(), TicketStatus.AVAILABLE, ticket.getZone());
        log.info("{} 공연 {}회차 {} 자리 매진이 아닌가? {}", ticket.getPerformanceId(), ticket.getSessionNum(), ticket.getZone(), isAvailableInZone);

        if (isAvailableInZone) {
            ticket.releaseToAvailable();
        } else {
            ticket.releaseToStandby();
        }
    }

    private @NonNull Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(TicketErrorCode.TICKET_NOT_FOUND));
    }

    @Override
    @Transactional
    public void standbyTicket(StandbyTicketRequest request) {
        Ticket ticket = getTicket(request.ticketId());
        ticket.standbyTicket(request.standbyUserId(), request.standbyExpiredAt());
    }

    private boolean isStandbyTicket(Ticket ticket, Long userId) {
        /* TODO : standby 논의 필요
         * StandBy 대기 순서 도래 → Ticket의 waitUserId를 변경 후, 고객에게 알림
         * 아니라면 어떻게 표시될 지 논의 필요.
         * 현행안 by.phb : Ticket이 취소표고 대기자가 user와 동일 할 경우, 대기자의 표임을 표시
         * */

        return ticket.getTicketStatus() == TicketStatus.CANCELED &&
               ticket.getStandbyUserId().equals(userId) &&
               ticket.getStandbyExpiredAt().isBefore(LocalDateTime.now())
        ;
    }

    private boolean isPossibleToHold(Ticket ticket) {
        return ticket.getTicketStatus() == TicketStatus.AVAILABLE;
    }
}
