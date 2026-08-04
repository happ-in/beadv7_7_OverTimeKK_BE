package com.programmers.kdt.standby.scheduler;

import com.programmers.kdt.standby.service.StandbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class StandbyExpirationScheduler {

    private final StandbyService standbyService;

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void expireHeldStandbys() {
        int expiredCount = standbyService.expireHeldStandbys();

        if (expiredCount > 0) {
            log.info("결제 미완료로 만료된 대기 {} 건 처리", expiredCount);
        }
    }
}
