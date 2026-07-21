package com.programmers.kdt.payment.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_refund")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefund extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;

    @Column(name = "reason")
    private String reason;

    public static PaymentRefund create(Long paymentId, Long refundAmount, String reason) {
        if (paymentId == null) {
            throw new IllegalArgumentException("결제가 존재하지 않습니다.");
        }
        if (refundAmount == null || refundAmount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0원보다 커야 합니다.");
        }
        PaymentRefund refund = new PaymentRefund();
        refund.paymentId = paymentId;
        refund.refundAmount = refundAmount;
        refund.reason = reason;
        return refund;
    }
}
