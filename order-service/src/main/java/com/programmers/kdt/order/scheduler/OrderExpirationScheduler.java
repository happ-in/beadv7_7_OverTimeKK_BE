package com.programmers.kdt.order.scheduler;

import com.programmers.kdt.order.service.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OrderExpirationScheduler {

    private final OrderServiceImpl orderServiceImpl;

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void expireOrder(){
        orderServiceImpl.expireOrders();
    }
}
