package com.programmers.kdt.payment.client.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// 기능 테스트를 위한 임시 구현체
public class SpringRefundEventPublisher implements RefundEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(RefundRequestEvent event) {
        eventPublisher.publishEvent(event);
    }
}
