package com.programmers.kdt.payment.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
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

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "refunded_amount", nullable = false)
    private Long refundedAmount;

    @Column(name = "payment_key")
    private String paymentKey; // PG 참조값

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    //결제 생성 메서드
    public static Payment create(Long orderId, Long userId, Long amount) {

        if (orderId == null) { // 어떤 예외처리 ?
            throw new BusinessException(PaymentErrorCode.MISSING_ORDER_ID);
        }

        if (amount == null || amount <= 0) {
            throw new BusinessException(PaymentErrorCode.ZERO_PAYMENT_AMOUNT, amount);
        }
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.userId = userId;
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
        if (isReady()) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS, this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.PAID;
    }

    //PG사 키 등록
    public void assignPaymentKey(String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_KEY, paymentKey);
        }
        this.paymentKey = paymentKey;
    }

    // 결제 실패 메서드 READY -> FAILED
    public void fail() {
        if (paymentStatus == PaymentStatus.FAILED) {
            return; // 이미 취소된 경우 중복 이벤트 무시
        }
        if (isReady()) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS, this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }

    // 전액 취소 메서드 PAID -> CANCELLED
    public void refund() {
        if (paymentStatus == PaymentStatus.CANCELLED) {
            return; // 이미 취소 완료된 상태, 중복 이벤트 무시
        }
        if (isPaidOrPartialCancelled()) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS, this.paymentStatus);
        }
        partialRefund(amount - refundedAmount); // 부분환불에 로직 위임 -> refundedAmount 부분환불 로직에서만 변경
    }

    // 부분 취소 메서드 PAID OR PARTIAL_CANCELLED -> PARTIAL_CANCELLED
    public void partialRefund(Long cancelAmount) {
        if (isPaidOrPartialCancelled()) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS, this.paymentStatus);
        }

        long remaining = amount - refundedAmount; // 남은 금액 = 결제금액 - 취소금액
        if (cancelAmount == null || cancelAmount <= 0 || cancelAmount > remaining) {
            throw new BusinessException(PaymentErrorCode.INVALID_REFUND_AMOUNT, refundedAmount, cancelAmount);
        }

        this.refundedAmount += cancelAmount;
        this.paymentStatus = (this.refundedAmount.equals(amount))
                ? PaymentStatus.CANCELLED // 환불금과 총액이 같은 경우
                : PaymentStatus.PARTIAL_CANCELLED;
    }

    // READY 상태인지 판별
    private boolean isReady() {
        return paymentStatus != PaymentStatus.READY;
    }

    // PAID 또는 PARTIAL_CANCELLED 상태인지 판별
    private boolean isPaidOrPartialCancelled() {
        return paymentStatus != PaymentStatus.PAID && paymentStatus != PaymentStatus.PARTIAL_CANCELLED;
    }
}
