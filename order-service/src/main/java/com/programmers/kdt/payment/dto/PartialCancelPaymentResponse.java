package com.programmers.kdt.payment.dto;

import com.programmers.kdt.payment.entity.Payment;

public record PartialCancelPaymentResponse(
        Long paymentId,
        String status,
        Long refundedAmount
) {
    public static PartialCancelPaymentResponse from(Payment payment) {
        return new PartialCancelPaymentResponse(
                payment.getId(),
                payment.getPaymentStatus().name(),
                payment.getRefundedAmount()
        );
    }
}
