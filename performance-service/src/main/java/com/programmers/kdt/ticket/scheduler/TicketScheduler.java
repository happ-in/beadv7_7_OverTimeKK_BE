package com.programmers.kdt.ticket.scheduler;

import com.programmers.kdt.ticket.service.ChangeTicketStatusService;
import com.programmers.kdt.ticket.dto.SessionZoneKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketScheduler {

    private final ChangeTicketStatusService changeTicketStatusService;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void releaseExpiredHoldTickets() {
        List<Long> expiredTicketIds = changeTicketStatusService.findExpiredHoldTicketIds();
        if (expiredTicketIds.isEmpty()) {
            return;
        }

        Map<SessionZoneKey, Boolean> zoneAvailabilityCache = new HashMap<>();
        int successCount = 0;

        for (Long ticketId : expiredTicketIds) {
            try {
                changeTicketStatusService.releaseExpiredHoldTicket(ticketId, zoneAvailabilityCache);
                successCount++;
            } catch (Exception e) {
                log.error("티켓 점유 해지 실패, ticket_id : {}", ticketId, e);
            }
        }

        log.info("점유시간 만료 티켓 {} / {} 건 자동 해제", successCount, expiredTicketIds.size());
    }
}
