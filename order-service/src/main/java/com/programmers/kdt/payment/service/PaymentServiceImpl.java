package com.programmers.kdt.payment.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.repository.OrderRepository;
import com.programmers.kdt.payment.client.pg.*;
import com.programmers.kdt.payment.client.refund.*;
import com.programmers.kdt.payment.dto.*;
import com.programmers.kdt.payment.entity.Payment;
import com.programmers.kdt.payment.entity.PaymentRefund;
import com.programmers.kdt.payment.entity.PaymentStatus;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.repository.PaymentRefundRepository;
import com.programmers.kdt.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final RefundEventPublisher refundEventPublisher;
    private final PerformanceClient performanceClient;
    private final OrderClient orderClient;
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

    // 환불 요청(접수)
    @Transactional
    public RefundPaymentResponse refund(Long paymentId, RefundPaymentRequest request) {
        Payment payment = getPayment(paymentId);

        try {
            payment.requestRefund();
            paymentRepository.saveAndFlush(payment);
        } catch(ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CONCURRENT_MODIFICATION);
        }

        refundEventPublisher.publish(new RefundRequestEvent(payment.getId(), request.reason(), LocalDateTime.now()));

        return RefundPaymentResponse.from(payment);
    }

    // 환불 처리(PG사 호출)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 기존 트랜잭션을 보류시키고 새로운 트랜잭션을 생성
    public void onRefundRequested(RefundRequestEvent event) {
        Payment payment = getPayment(event.paymentId());

        try {
            Long ticketId = orderClient.getTicketId(payment.getOrderId());
            LocalDate performanceDate = performanceClient.getPerformanceDate(ticketId);
            double refundRate = RefundPolicy.resolveRefundRate(performanceDate, LocalDate.now());

            if (refundRate == 0.0) {
                payment.failRefund();
                paymentRepository.save(payment);
                log.warn("당일/이후 환불 요청으로 환불 불가 - paymentId={}", payment.getId());
                return;
            }

            Long refundAmount = RefundPolicy.calculateRefundAmount(payment.getAmount(), refundRate);

            PgCancelResult cancelResult = pgClient.cancel(
                    new PgCancelCommand(payment.getPaymentKey(), refundAmount, event.reason()));

            if (!cancelResult.success()) {
                payment.failRefund();
                paymentRepository.save(payment);
                log.error("PG 취소 실패 - paymentId={}", payment.getId());
                return;
            }

            paymentRefundRepository.save(PaymentRefund.create(payment.getId(), refundAmount, event.reason()));
            payment.completeRefund(refundAmount);
        } catch (Exception e) {
            payment.failRefund();
            paymentRepository.save(payment);
            log.error("환불 처리 중 예외 발생 - paymentId={}", payment.getId(), e);
        }
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
