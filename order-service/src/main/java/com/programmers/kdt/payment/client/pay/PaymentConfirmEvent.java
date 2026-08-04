package com.programmers.kdt.payment.client.pay;

public record PaymentConfirmEvent(
    Long orderId,
    Long paymentId
) {

}
