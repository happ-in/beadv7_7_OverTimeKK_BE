package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.RegisterPerformanceRequest;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import com.programmers.kdt.performance.event.PerformanceCacheEvictEvent;
import com.programmers.kdt.performance.event.PerformanceSeatPriceRegisterEvent;
import com.programmers.kdt.performance.event.PerformanceSessionRegisterEvent;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.ticket.entity.TicketStatus;
import com.programmers.kdt.ticket.event.TicketIssueEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PerformanceV2Service {

    private final PerformanceRepository performanceRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void registerPerformanceInformation(RegisterPerformanceRequest request, Long sellerId) {
        // 1. 공연등록
        Performance performance = performanceRepository.save(request.toPerformance(sellerId));

        // 2. 회차등록
        publisher.publishEvent(new PerformanceSessionRegisterEvent(performance, request.sessionRequests()));

        // 3. 좌석 등급별 금액 등록
        publisher.publishEvent(new PerformanceSeatPriceRegisterEvent(performance, request.seatPriceRequests()));

        // 4. 캐시삭제
        publisher.publishEvent(new PerformanceCacheEvictEvent("performanceList", "all"));

        // 5. Ticket
        publisher.publishEvent(new TicketIssueEvent(performance.getPerformanceId(), TicketStatus.AVAILABLE));
    }

    @Transactional
    public int closedPerformance() {
        return performanceRepository.updateExpiredPerformances(PerformanceStatus.CLOSED, LocalDate.now());
    }
}
