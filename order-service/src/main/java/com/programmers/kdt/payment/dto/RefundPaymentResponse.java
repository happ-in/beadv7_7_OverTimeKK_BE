package com.programmers.kdt.payment.dto;

import com.programmers.kdt.payment.entity.Payment;

public record RefundPaymentResponse(
        Long paymentId,
        String status,
        Long refundedAmount
) {
    public static RefundPaymentResponse from(Payment payment) {
        return new RefundPaymentResponse(
                payment.getId(),
                payment.getPaymentStatus().name(),
                payment.getRefundedAmount()
        );
    }
}
