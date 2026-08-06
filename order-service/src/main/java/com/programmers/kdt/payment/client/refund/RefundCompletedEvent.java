package com.programmers.kdt.payment.client.refund;

public record RefundCompletedEvent(
        Long orderId,
        Long paymentId
) {
}
