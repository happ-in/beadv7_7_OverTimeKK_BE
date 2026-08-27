package com.programmers.kdt.payment.service.tx;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.payment.entity.Payment;
import com.programmers.kdt.payment.entity.PaymentStatus;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentTxOps {

    private final PaymentRepository paymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment assignKeyAndCommit(Long paymentId, String transactionKey) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(PaymentErrorCode.INVALID_PAYMENT_STATUS, payment.getPaymentStatus());
        }

        payment.assignPaymentKey(transactionKey);
        payment.markPending();
        try {
            paymentRepository.saveAndFlush(payment);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CONCURRENT_MODIFICATION);
        }
        return payment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment applyConfirmResult(Long paymentId, PgOutcome pgOutcome) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        switch (pgOutcome) {
            case SUCCESS -> payment.confirmVerifiedSuccess();
            case EXPLICIT_FAIL -> payment.confirmVerifiedFail();
            case AMBIGUOUS -> {}
        }

        try {
            paymentRepository.saveAndFlush(payment);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CONCURRENT_MODIFICATION);
        }

        return payment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment applyReconcileResult(Long paymentId, PgOutcome pgOutcome) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        switch (pgOutcome) {
            case SUCCESS -> payment.confirmVerifiedSuccess();
            case EXPLICIT_FAIL -> payment.confirmVerifiedFail();
            case AMBIGUOUS -> {return payment;}
        }

        try {
            paymentRepository.saveAndFlush(payment);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(PaymentErrorCode.PAYMENT_CONCURRENT_MODIFICATION);
        }

        return payment;
    }
}


