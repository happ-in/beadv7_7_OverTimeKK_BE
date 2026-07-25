package com.programmers.kdt.ticket.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.ticket.dto.CreateStandbyResponse;
import com.programmers.kdt.standby.repository.StandbyRepository;
import com.programmers.kdt.ticket.dto.SessionStartDateResponse;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final StandbyRepository standbyRepository;
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
}
