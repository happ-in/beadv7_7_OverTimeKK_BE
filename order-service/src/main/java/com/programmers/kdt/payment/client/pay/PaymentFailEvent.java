package com.programmers.kdt.payment.client.pay;

public record PaymentFailEvent(
        Long orderId,
        Long paymentId,
        String reason
) {
}
