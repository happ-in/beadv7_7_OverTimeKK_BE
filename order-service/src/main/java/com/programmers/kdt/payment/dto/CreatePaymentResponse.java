package com.programmers.kdt.payment.dto;

import com.programmers.kdt.payment.client.PgReadyResult;
import com.programmers.kdt.payment.entity.Payment;
import com.programmers.kdt.payment.entity.PaymentStatus;

public record CreatePaymentResponse(
        Long paymentId,
        String status,
        String transactionKey,
        String redirectionUrl
) {

    public static CreatePaymentResponse of(Payment payment, PgReadyResult readyResult) {
        return new CreatePaymentResponse(
                payment.getId(), payment.getPaymentStatus().name(),
                readyResult.transactionKey(), readyResult.redirectionUrl()
        );
    }
}
