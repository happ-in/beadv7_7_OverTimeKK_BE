package com.programmers.kdt.payment.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "refunded_amount", nullable = false)
    private Long refundedAmount;

    //결제 생성 메서드
    public static Payment create(Long orderId, Long amount) {

        if (orderId == null) {
            throw new IllegalArgumentException("주문이 존재하지 않습니다.");
        }

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0원보다 커야 합니다. 입력값 = " + amount);
        }
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.paymentStatus = PaymentStatus.READY; //상태는 READY로 생성
        payment.refundedAmount = 0L; //환불금액은 0
        return payment;
    }

    //결제 승인 메서드 READY -> PAID
    public void approve() {
        if (paymentStatus == PaymentStatus.PAID) {
            return; // 이미 결제된 경우 중복 이벤트 무시
        }
        if (paymentStatus != PaymentStatus.READY) {
            throw new IllegalStateException("READY 상태에서만 결제 승인 가능합니다. 현재 상태 :" + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.PAID;
    }

    // 결제 실패 메서드 READY -> FAILED
    public void fail() {
        if (paymentStatus == PaymentStatus.FAILED) {
            return; // 이미 취소된 경우 중복 이벤트 무시
        }
        if (paymentStatus != PaymentStatus.READY) {
            throw new IllegalStateException("READY 상태에서만 결제 실패 처리 가능합니다. 현재 상태 :" + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }

    // 전액 취소 메서드 PAID -> CANCELLED
    public void cancel() {
        if (paymentStatus == PaymentStatus.CANCELLED) {
            return; // 이미 취소 완료된 상태, 중복 이벤트 무시
        }
        if (paymentStatus != PaymentStatus.PAID && paymentStatus != PaymentStatus.PARTIAL_CANCELLED) {
            throw new IllegalStateException("PAID 또는 PARTIAL_CANCELLED 상태에서만 환불이 가능합니다. 현재 상태 :" + this.paymentStatus);
        }
        partialCancel(amount - refundedAmount); // 부분환불에 로직 위임 -> refundedAmount 부분환불 로직에서만 변경
    }

    // 부분 취소 메서드 PAID OR PARTIAL_CANCELLED -> PARTIAL_CANCELLED
    public void partialCancel(Long cancelAmount) {
        if (paymentStatus != PaymentStatus.PAID && paymentStatus != PaymentStatus.PARTIAL_CANCELLED) {
            throw new IllegalStateException("PAID 또는 PARTIAL_CANCELLED 상태에서만 부분 환불이 가능합니다. 현재 상태 :" + this.paymentStatus);
        }

        long remaining = amount - refundedAmount; // 남은 금액 = 결제금액 - 취소금액
        if (cancelAmount == null || cancelAmount <= 0 || cancelAmount > remaining) {
            throw new IllegalArgumentException("부분 환불 금액이 유효하지 않습니다. 잔여 금액 : " + remaining + "요청 금액 :" + cancelAmount);
        }

        this.refundedAmount += cancelAmount;
        this.paymentStatus = (this.refundedAmount.equals(amount))
                ? PaymentStatus.CANCELLED // 환불금과 총액이 같은 경우
                : PaymentStatus.PARTIAL_CANCELLED;
    }
}
