package com.programmers.kdt.payment.client.pay;

public interface OrderCompletionEventPublisher {
    void publish(PaymentConfirmEvent event);
}
