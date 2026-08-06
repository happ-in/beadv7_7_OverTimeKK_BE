package com.programmers.kdt.payment.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.repository.OrderRepository;
import com.programmers.kdt.payment.client.pay.OrderCompletionEventPublisher;
import com.programmers.kdt.payment.client.pay.PaymentConfirmEvent;
import com.programmers.kdt.payment.client.pg.*;
import com.programmers.kdt.payment.client.refund.*;
import com.programmers.kdt.payment.dto.*;
import com.programmers.kdt.payment.entity.Payment;
import com.programmers.kdt.payment.entity.PaymentRefund;
import com.programmers.kdt.payment.entity.PaymentStatus;
import com.programmers.kdt.payment.entity.RefundPolicy;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.exception.PointErrorCode;
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
import java.util.function.Supplier;


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
    private final PointService pointService;
    private final OrderCompletionEventPublisher orderCompletionEventPublisher;



    // 결제 생성
    @Transactional
    public CreatePaymentResponse pay(CreatePaymentRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        order.startPayment(LocalDateTime.now()); // 주문상태 검증

        // 주문 금액이 같은지 판별
        if (!order.getTotalAmount().equals(request.amount())) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 사용 포인트 검증 및 차감 (PG 결제 금액 = 주문 금액 - 사용 포인트)
        Long usedPoint = request.usedPointOrZero();
        if (usedPoint > request.amount() || usedPoint < 0) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 아직 포인트 전액 결제는 고려하지 않은 상태.
        Long pgAmount = request.amount() - usedPoint;

        if (usedPoint > 0) {
            pointService.usePoint(order.getUserId(), usedPoint, pointUseEventId(request.orderId()));
        }

        Payment payment = Payment.create(order.getOrderId(), order.getUserId(), request.amount());

        // 나중에 PG사 요청은 트랜잭션에서 빼는 것을 고려(MVP 제외)
        PgReadyResult readyResult = callPg("토스 결제 준비", request.orderId(),
                () -> pgClient.ready(new PgReadyCommand(request.orderId(), pgAmount)));

        payment.assignPgOrderId(readyResult.orderId());
        paymentRepository.save(payment);

        return CreatePaymentResponse.of(payment, readyResult);
    }

    // 결제 확인
    @Transactional
    public ConfirmPaymentResponse confirm(Long paymentId, ConfirmPaymentRequest request) {
        Payment payment = getPayment(paymentId);

        // 상태 검증
        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS, payment.getPaymentStatus());
        }

        payment.assignPaymentKey(request.transactionKey());
        Long usedPoint = getUsedPointForOrder(payment.getOrderId());
        Long pgApproveAmount = payment.getAmount() - usedPoint;

        PgApproveResult approveResult = callPg("토스 결제 승인", paymentId,
                () -> pgClient.approve(new PgApproveCommand(payment.getPaymentKey(), payment.getPgOrderId(), pgApproveAmount)));

        // 결제 요청 성공 & 실패 분기
        if (approveResult.success()) {
            payment.approve();
            orderCompletionEventPublisher.publish(new PaymentConfirmEvent(payment.getOrderId(), payment.getId()));
        } else {
            payment.fail();
            rollbackFailedPoint(payment.getOrderId(), usedPoint);
        }

        return ConfirmPaymentResponse.from(payment);
    }

    private Long resolveUsedPoint(String eventId) {
        Long usedPoint = pointService.findUsedAmount(eventId);
        return usedPoint == null ? 0L : usedPoint;
    }

    // 결제 실패 요청
    @Transactional
    public FailPaymentResponse fail(Long paymentId, FailPaymentRequest request) {
        Payment payment = getPayment(paymentId);

        boolean alreadyFailed = payment.getPaymentStatus() == PaymentStatus.FAILED;
        payment.fail();

        if (!alreadyFailed) {
            if (payment.getPaymentKey() != null) {
                callPg("토스 결제 취소", paymentId,
                        () -> pgClient.cancel(new PgCancelCommand(payment.getPaymentKey(), payment.getAmount(), request.reason())));
            }

            Long usedPoint = getUsedPointForOrder(payment.getOrderId());
            rollbackFailedPoint(payment.getOrderId(), usedPoint);
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
    public RefundPaymentResponse refund(Long orderId, RefundPaymentRequest request) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 환불 가능 기간이 지났으면 접수 거부
        Long ticketId = orderClient.getTicketId(orderId);
        LocalDate performanceDate = performanceClient.getPerformanceDate(ticketId);
        double refundRate = RefundPolicy.resolveRefundRate(performanceDate, LocalDate.now());
        if (refundRate == 0.0) {
            throw new BusinessException(PaymentErrorCode.REFUND_PERIOD_EXPIRED);
        }

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
        boolean refundCompleted = false;
        String failReason = null;

        try {
            Long ticketId = orderClient.getTicketId(payment.getOrderId());
            LocalDate performanceDate = performanceClient.getPerformanceDate(ticketId);
            double refundRate = RefundPolicy.resolveRefundRate(performanceDate, LocalDate.now());

            if (refundRate == 0.0) {
                failReason = PaymentErrorCode.REFUND_PERIOD_EXPIRED.toString();
            } else {
                Long refundAmount = RefundPolicy.calculateRefundAmount(payment.getAmount(), refundRate);
                PgCancelResult cancelResult = pgClient.cancel(
                        new PgCancelCommand(payment.getPaymentKey(), refundAmount, event.reason()));

                if (!cancelResult.success()) {
                    failReason = PaymentErrorCode.PG_REQUEST_FAILED.toString();
                } else {
                    paymentRefundRepository.save(PaymentRefund.create(payment.getId(), refundAmount, event.reason()));
                    payment.completeRefund(refundAmount);
                    refundCompleted = true;

                    Long usedPoint = getUsedPointForOrder(payment.getOrderId());
                    if (usedPoint > 0) {
                        rollbackRefundPointWithRetry(payment.getOrderId(), usedPoint, refundRate, payment.getId());
                    }
                }
            }
        } catch (Exception e) {
            if (refundCompleted) {
                // 환불은 이미 확정, 포인트 환급이 실패되더라도 환불은 완료로 처리
                log.error("[REFUND_POST_PROCESS_FAILED] 환불은 완료됐으나 후처리 실패 - paymentId={}", payment.getId(), e);
            } else {
                failReason = PaymentErrorCode.REFUND_SERVICE_FAILED.toString();
                log.error("환불 처리 중 예외 발생 - paymentID={}", payment.getId());
            }
        }

        if (refundCompleted) {
            refundEventPublisher.publishCompleted(
                    new RefundCompletedEvent(payment.getOrderId(), payment.getId())
            );
        } else {
            payment.failRefund();
            paymentRepository.save(payment);
            log.warn("환불 실패 - paymentId={}, reason={}", payment.getId(), failReason);
            refundEventPublisher.publishFailed(
                    new RefundFailedEvent(payment.getOrderId(), payment.getId(), failReason)
            );
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

    // PG사 호출
    private <T> T callPg(String action, Object contextId, Supplier<T> pgCall) {
        try {
            return pgCall.get();
        } catch (PgClientException e) {
            log.error("{} 실패 - id={}, pgCode={}, pgMessage={}", action, contextId, e.getPgErrorCode(), e.getMessage(), e);
            throw new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED);
        } catch (Exception e) {
            log.error("PG 요청 중 알 수 없는 오류 - action={}, id={}", action, contextId, e);
            throw new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED);
        }
    }

    private Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private Long getUsedPointForOrder(Long orderId) {
        return resolveUsedPoint(pointUseEventId(orderId));
    }

    private String pointUseEventId(Long orderId) {
        return "ORDER:" + orderId + ":POINT_USE";
    }

    private String pointRollbackFailEventId(Long orderId) {
        return "ORDER:" + orderId + ":POINT_ROLLBACK_FAIL";
    }

    private String pointRollbackRefundEventId(Long orderId) {
        return "ORDER:" + orderId + ":POINT_ROLLBACK_REFUND";
    }

    private void rollbackFailedPoint(Long orderId, Long usedPoint) {
        if (usedPoint > 0) {
            pointService.rollbackPoint(pointUseEventId(orderId), usedPoint,
                    pointRollbackFailEventId(orderId), true);
        }
    }

    // 재시도
    private void rollbackRefundPointWithRetry(Long orderId, Long usedPoint, double refundRate, Long paymentId) {
        Long refundedPoint = RefundPolicy.calculateRefundAmount(usedPoint, refundRate);
        if (refundedPoint <= 0) {
            return;
        }

        boolean isFullRollback = refundedPoint.equals(usedPoint);
        String originEventId = pointUseEventId(orderId);
        String rollbackEventId = pointRollbackRefundEventId(orderId);

        int maxAttempts = 3; // 최대 재시도 횟수
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                pointService.rollbackPoint(originEventId, refundedPoint, rollbackEventId, isFullRollback);
                return;
            } catch (BusinessException e) {
                    boolean retryable = e.getErrorCode() == PointErrorCode.POINT_CONCURRENT_MODIFICATION;
                if (!retryable || attempt == maxAttempts) {
                    // 환불은 이미 완료된 상태이지만 포인트는 수동 보정이 필요한 상태이므로, 로그를 남김
                    // 후에 메시지 큐를 만들어서 배치가 주기적으로 재시도하는 구조로 리팩토링 가능
                    log.error("[POINT_REFUND_RECONCILIATION_NEEDED] 환불은 완료됐지만 포인트 환급에 실패했습니다. " +
                                    "paymentId={}, orderId={}, amount={}, errorCode={}",
                            paymentId, orderId, refundedPoint, e.getErrorCode(), e);
                    return;
                }
                log.warn("포인트 환급 동시성 충돌, 재시도 {}회차 - orderId={}", attempt, orderId);
            }
        }
    }
}
