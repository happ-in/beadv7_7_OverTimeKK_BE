package com.programmers.kdt.performance.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSeatPrice;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.performance.repository.PerformanceSeatPriceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.exception.TicketErrorCode;
import com.programmers.kdt.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceV2Service {

    private final PerformanceRepository performanceRepository;
    private final PerformanceSessionRepository sessionRepository;
    private final PerformanceSeatPriceRepository seatPriceRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public void registerPerformanceInformation(RegisterPerformanceRequest request) {
        // 1. 공연등록
        Performance performance = performanceRepository.save(request.toPerformance());

        // 2. 회차등록
        List<PerformanceSession> sessions = request.toSessions(performance);
        sessionRepository.saveAll(sessions);

        // 3. 좌석 등급별 금액 등록
        List<PerformanceSeatPrice> seatPrices = request.toSeatPrices(performance);
        seatPriceRepository.saveAll(seatPrices);

        // 4. Ticket
        // TODO : 이벤트로 분리하기
        int issueTicketCount = ticketRepository.issueTicket(performance.getPerformanceId(), TicketStatus.AVAILABLE);
        if (issueTicketCount <= 0) {
            throw new BusinessException(TicketErrorCode.TICKET_ISSUE_FAILED);
        }
    }

    @Transactional
    public int closedPerformance() {
        return performanceRepository.updateExpiredPerformances(PerformanceStatus.CLOSED, LocalDate.now());
    }
}
