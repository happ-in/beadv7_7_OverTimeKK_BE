package com.programmers.kdt.payment.client.refund;

public record RefundFailedEvent(
        Long orderId,
        Long paymentId,
        String reason
) {
}
