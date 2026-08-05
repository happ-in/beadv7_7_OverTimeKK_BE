package com.programmers.kdt.settlement.scheduler;

import com.programmers.kdt.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final SettlementService settlementService;

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    public void createdMonthlySettlement(){
        LocalDate settlementMonth = LocalDate.now()
                .minusMonths(1)
                .withDayOfMonth(1);
        settlementService.createMonthlySettlements(settlementMonth);
    }
}
