package com.programmers.kdt.payment.client.pay;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringOrderCompletionEventPublisher implements OrderCompletionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(PaymentConfirmEvent event) {
        eventPublisher.publishEvent(event);
    }
}

