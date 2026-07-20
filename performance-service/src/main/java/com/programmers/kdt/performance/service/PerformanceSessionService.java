package com.programmers.kdt.performance.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.common.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.dto.PerformanceSessionRequest;
import com.programmers.kdt.performance.dto.PerformanceSessionResponse;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceSessionService {

    private final PerformanceSessionRepository sessionRepository;
    private final PerformanceRepository performanceRepository;

    @Transactional
    public PerformanceSessionResponse registerPerformanceSession(PerformanceSessionRequest request) {
        Performance performance = getPerformance(request.performanceId());

        PerformanceSession session = sessionRepository.save(request.toEntity(performance));

        return PerformanceSessionResponse.from(session);
    }

    @Transactional
    public PerformanceSessionResponse changePerformanceSession(PerformanceSessionRequest request) {
        PerformanceSession session = getPerformanceSession(new PerformanceSessionId(request.sessionId(), request.performanceId()));
        session.changePerformanceSession(request.actor(), request.performanceStartAt());
        return PerformanceSessionResponse.from(session);
    }

    // TODO: 공연 시작 여부를 검증한 후 공연 회차를 삭제하도록 구현
    @Transactional
    public void deletePerformanceSession(Long sessionNum, Long performanceId) {
        PerformanceSession session = getPerformanceSession(new PerformanceSessionId(sessionNum, performanceId));
        throw new UnsupportedOperationException("공연 회차별 삭제 구현 예정");
    }

    // TODO : 공연에 속한 모든 공연 회차 삭제 구현
    @Transactional
    public void deletePerformanceSessions(Long performanceId) {
        throw new UnsupportedOperationException("공연별 회차 전체 삭제 구현 예정");
    }

    public List<PerformanceSessionResponse> findAllPerformanceSessionsByPerformanceId(Long performanceId) {
        List<PerformanceSession> sessions = sessionRepository.findByPerformanceSessionId_PerformanceId(performanceId)
                .orElseThrow(() -> new BusinessException(PerformanceErrorCode.PERFORMANCE_SESSION_NOT_FOUND));

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


}
