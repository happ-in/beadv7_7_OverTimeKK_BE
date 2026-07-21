package com.programmers.kdt.payment.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.repository.OrderRepository;
import com.programmers.kdt.payment.client.*;
import com.programmers.kdt.payment.dto.*;
import com.programmers.kdt.payment.entity.Payment;
import com.programmers.kdt.payment.entity.PaymentRefund;
import com.programmers.kdt.payment.entity.PaymentStatus;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.repository.PaymentRefundRepository;
import com.programmers.kdt.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PgClient pgClient;

    // 결제 생성
    @Transactional
    public CreatePaymentResponse pay(CreatePaymentRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 주문 금액이 같은지 판별
        if (!order.getTotalAmount().equals(request.amount())) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        Payment payment = Payment.create(order.getOrderId(), order.getUserId(), request.amount());

        // 나중에 PG사 요청은 트랜잭션에서 빼는 것을 고려
        PgReadyResult readyResult;
        try {
            readyResult = pgClient.ready(new PgReadyCommand(request.orderId(), request.amount()));
        } catch (Exception e) {
            throw new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED);
        }

        // PG사 키 할당
        payment.assignPaymentKey(readyResult.transactionKey());
        paymentRepository.save(payment);

        return CreatePaymentResponse.of(payment, readyResult);
    }

    // 결제 확인
    @Transactional
    public ConfirmPaymentResponse confirm(Long paymentId, ConfirmPaymentRequest request) {
        Payment payment = getPayment(paymentId);

        // 요청한 결제와 다른 결제일 경우
        if (!payment.getPaymentKey().equals(request.transactionKey())) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_KEY_MISMATCH);
        }

        // 상태 검증
        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS, payment.getPaymentStatus());
        }

        PgApproveResult approveResult;
        try {
            approveResult = pgClient.approve(new PgApproveCommand(payment.getPaymentKey(), payment.getAmount()));
        } catch (Exception e) {
            throw new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED);
        }

        // 결제 요청 성공 & 실패 분기
        if (approveResult.success()) {
            payment.approve();
        } else {
            payment.fail();
        }

        return ConfirmPaymentResponse.from(payment);
    }

    // 결제 실패 요청
    @Transactional
    public FailPaymentResponse fail(Long paymentId, FailPaymentRequest request) {
        Payment payment = getPayment(paymentId);

        boolean alreadyFailed = payment.getPaymentStatus() == PaymentStatus.FAILED;
        payment.fail();

        if (!alreadyFailed && payment.getPaymentKey() != null) {
            try {
                pgClient.cancel(new PgCancelCommand(payment.getPaymentKey(), payment.getAmount(), request.reason()));
            } catch (Exception e) {
                throw new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED);
            }

        }

        return FailPaymentResponse.from(payment);
    }

    // 결제 내역 조회
    @Transactional(readOnly = true)
    public Page<GetPaymentHistoryResponse> getPaymentHistory(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable)
                .map(GetPaymentHistoryResponse::from);
    }

    // 전액 환불
    @Transactional
    public RefundPaymentResponse refund(Long paymentId, RefundPaymentRequest request) {
        Payment payment = getPayment(paymentId);

        // 환불금 계산
        Long refundAmount = payment.getAmount() - payment.getRefundedAmount();

        try {
            payment.refund();
            paymentRepository.saveAndFlush(payment);
        } catch(ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CONCURRENT_MODIFICATION);
        }


        try {
            pgClient.cancel(new PgCancelCommand(payment.getPaymentKey(), refundAmount, request.reason()));
        } catch (Exception e) {
            throw new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED); // 예외 → 롤백 → DB 상태 원복
        }

        paymentRefundRepository.save(PaymentRefund.create(payment.getId(), refundAmount, request.reason()));
        return RefundPaymentResponse.from(payment);
    }

    // 부분 환불
    @Transactional
    public PartialRefundPaymentResponse partialRefund(Long paymentId, PartialRefundPaymentRequest request) {
        Payment payment = getPayment(paymentId);

        try {
            payment.partialRefund(request.amount());
            paymentRepository.saveAndFlush(payment); // 미리 flush를 통해 버전이 일치하는지 검증
        } catch (ObjectOptimisticLockingFailureException e) { // 충돌 발생 시
            throw new BusinessException(PaymentErrorCode.PAYMENT_CONCURRENT_MODIFICATION);
        }

        try {
            pgClient.cancel(new PgCancelCommand(payment.getPaymentKey(), request.amount(), request.reason()));
        } catch (Exception e) {
            throw new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED); // 예외 → 롤백 → DB 상태 원복
        }

        paymentRefundRepository.save(PaymentRefund.create(payment.getId(), request.amount(), request.reason()));

        return PartialRefundPaymentResponse.from(payment);
    }

    // 환불 내역 조회
    @Transactional(readOnly = true)
    public Page<GetPaymentRefundHistoryResponse> getRefundHistory (Long paymentId, Pageable pageable) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        return paymentRefundRepository.findByPaymentId(paymentId, pageable)
                .map(GetPaymentRefundHistoryResponse::from);
    }

    private Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
