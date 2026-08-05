package com.programmers.kdt.settlement.dto;

public record PaymentResponse(
        Long orderId,
        Long amount,
        Long refundedAmount
) {
    public Long netAmount(){
        return amount - refundedAmount;
    }
}
