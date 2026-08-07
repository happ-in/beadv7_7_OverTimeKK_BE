package com.programmers.kdt.payment.client.pay;

public interface PaymentResultEventPublisher {
    void publishConfirmed(PaymentConfirmEvent event);
    void publishFailed(PaymentFailEvent event);
}
