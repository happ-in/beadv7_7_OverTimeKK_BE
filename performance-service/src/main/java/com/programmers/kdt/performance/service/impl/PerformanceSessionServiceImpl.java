package com.programmers.kdt.performance.service.impl;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.dto.PerformanceSessionRequest;
import com.programmers.kdt.performance.dto.PerformanceSessionResponse;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.performance.service.PerformanceSessionService;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceSessionServiceImpl implements PerformanceSessionService {

    private final PerformanceSessionRepository sessionRepository;
    private final PerformanceRepository performanceRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public PerformanceSessionResponse registerPerformanceSession(PerformanceSessionRequest request) {
        Performance performance = getPerformance(request.performanceId());
        // TODO : 인증되어 들어온 판매자가 performance의 seller_id와 일치하는 검증 로직 필요
        PerformanceSession session = sessionRepository.save(request.toEntity(performance));

        ticketRepository.issueAdditionalTickets(request.performanceId(), request.sessionNum(), TicketStatus.AVAILABLE);

        return PerformanceSessionResponse.from(session);
    }

    @Transactional
    public PerformanceSessionResponse changePerformanceSession(PerformanceSessionRequest request) {
        PerformanceSession session = getPerformanceSession(new PerformanceSessionId(request.sessionNum(), request.performanceId()));
        session.changePerformanceSession(request.actor(), request.performanceStartAt());
        return PerformanceSessionResponse.from(session);
    }

    @Transactional
    public void deletePerformanceSession(Long sessionNum, Long performanceId) {
        PerformanceSessionId sessionId = new PerformanceSessionId(sessionNum, performanceId);
        PerformanceSession session = getPerformanceSession(sessionId);
        Performance performance = getPerformance(performanceId);
        validDeleteSessionTime(performance.getTicketOpenAt(), session.getPerformanceStartAt());
        sessionRepository.deleteById(sessionId);

        // 티켓 삭제
        // TODO: delCount = 0이면 Error 고민 필요
        ticketRepository.deleteByPerformanceIdAndSessionNum(performanceId, sessionNum);
    }

    @Transactional
    public void deletePerformanceSessions(Long performanceId) {
        Performance performance = getPerformance(performanceId);
        validateDeletableBeforeTicketOpen(performance.getTicketOpenAt());
        sessionRepository.deleteByPerformanceSessionId_PerformanceId(performanceId);
    }

    public List<PerformanceSessionResponse> findAllPerformanceSessionsByPerformanceId(Long performanceId) {
        List<PerformanceSession> sessions = sessionRepository.findByPerformanceSessionId_PerformanceId(performanceId);
        if (sessions.isEmpty()) {
            throw new BusinessException(PerformanceErrorCode.PERFORMANCE_SESSION_NOT_FOUND);
        }

        List<PerformanceSessionResponse> responses = new ArrayList<>(sessions.size());
        for (PerformanceSession session : sessions) {
            responses.add(PerformanceSessionResponse.from(session));
        }

        return responses;
    }

    public PerformanceSessionResponse getPerformanceSession(Long sessionNum, Long performanceId) {
        PerformanceSession session = getPerformanceSession(new PerformanceSessionId(sessionNum, performanceId));
        return PerformanceSessionResponse.from(session);
    }


    private PerformanceSession getPerformanceSession(PerformanceSessionId sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(PerformanceErrorCode.PERFORMANCE_SESSION_NOT_FOUND));
    }

    private Performance getPerformance(Long performanceId) {
        return performanceRepository.findById(performanceId).orElseThrow(
                () -> new BusinessException(PerformanceErrorCode.PERFORMANCE_NOT_FOUND)
        );
    }

    private void validDeleteSessionTime(LocalDateTime ticketOpenAt, LocalDateTime performanceStartAt) {
        validateDeletableBeforeTicketOpen(ticketOpenAt);
        validateDeletableBeforePerformanceStart(performanceStartAt);

    }

    private static void validateDeletableBeforePerformanceStart(LocalDateTime performanceStartAt) {
        if (performanceStartAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(PerformanceErrorCode.PERFORMANCE_SESSION_UPDATE_NOT_ALLOWED_AFTER_PERFORMANCE_START, "삭제");
        }
    }

    private static void validateDeletableBeforeTicketOpen(LocalDateTime ticketOpenAt) {
        if (ticketOpenAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(PerformanceErrorCode.PERFORMANCE_SESSION_UPDATE_NOT_ALLOWED_AFTER_TICKET_OPEN, "삭제");
        }
    }
}
