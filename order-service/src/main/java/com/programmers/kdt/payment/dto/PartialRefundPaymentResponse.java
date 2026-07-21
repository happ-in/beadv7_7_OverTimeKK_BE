package com.programmers.kdt.payment.dto;

import com.programmers.kdt.payment.entity.Payment;

public record PartialRefundPaymentResponse(
        Long paymentId,
        String status,
        Long refundedAmount
) {
    public static PartialRefundPaymentResponse from(Payment payment) {
        return new PartialRefundPaymentResponse(
                payment.getId(),
                payment.getPaymentStatus().name(),
                payment.getRefundedAmount()
        );
    }
}
