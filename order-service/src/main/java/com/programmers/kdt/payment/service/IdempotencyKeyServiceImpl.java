package com.programmers.kdt.payment.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.payment.entity.key.IdempotencyKey;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyKeyServiceImpl implements IdempotencyKeyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final IdempotencyKeyTxOps txOps;

    @Override
    public Optional<String> generate(String idempotencyKey, String requestHash) {
        try {
            txOps.tryInsert(idempotencyKey, requestHash);
            return Optional.empty();
        } catch (DataIntegrityViolationException e) { // 이미 있는 멱등키를 다시 저장하려고 하는 경우
            IdempotencyKey existing = txOps.findFresh(idempotencyKey)
                    .orElseThrow(() -> e);

            if (existing.isExpired()) {
                txOps.expireAndReinsert(idempotencyKey, existing, requestHash);
                return Optional.empty();
            }

            if (!existing.getRequestHash().equals(requestHash)) {
                throw new BusinessException(PaymentErrorCode.IDEMPOTENCY_KEY_CONFLICT);
            }

            if (existing.getResponseBody() == null) {
                throw new BusinessException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
            }

            return Optional.of(existing.getResponseBody());
        }
    }

    @Override
    @Transactional
    public void complete(String idempotencyKey, String responseBody) {
        int updated = idempotencyKeyRepository.findById(idempotencyKey)
                .map(key -> { key.complete(responseBody); return 1; })
                .orElse(0);
        log.info("[IDEMPOTENCY_COMPLETE] key={}, found={}, bodyLen={}", idempotencyKey, updated, responseBody == null ? -1 : responseBody.length());
    }

    @Override
    public void release(String idempotencyKey) {
        txOps.delete(idempotencyKey);
    }
}
