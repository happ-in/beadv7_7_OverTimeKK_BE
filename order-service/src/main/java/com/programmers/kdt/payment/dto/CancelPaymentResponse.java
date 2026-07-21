package com.programmers.kdt.payment.dto;

import com.programmers.kdt.payment.entity.Payment;

public record CancelPaymentResponse(
        Long paymentId,
        String status,
        Long refundedAmount
) {
    public static CancelPaymentResponse from(Payment payment) {
        return new CancelPaymentResponse(
                payment.getId(),
                payment.getPaymentStatus().name(),
                payment.getRefundedAmount()
        );
    }
}
