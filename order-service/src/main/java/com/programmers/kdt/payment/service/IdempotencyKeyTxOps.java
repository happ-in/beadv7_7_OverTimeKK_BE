package com.programmers.kdt.payment.service;

import com.programmers.kdt.payment.entity.key.IdempotencyKey;
import com.programmers.kdt.payment.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyTxOps {

    private static final Duration TTL = Duration.ofMinutes(5);
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryInsert(String idempotencyKey, String requestHash) {
        idempotencyKeyRepository.saveAndFlush(IdempotencyKey
                .generate(idempotencyKey, requestHash, LocalDateTime.now().plus(TTL)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<IdempotencyKey> findFresh(String idempotencyKey) {
        return idempotencyKeyRepository.findById(idempotencyKey);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireAndReinsert(String idempotencyKey, IdempotencyKey existing, String requestHash) {
        idempotencyKeyRepository.delete(existing);
        idempotencyKeyRepository.flush();
        idempotencyKeyRepository.saveAndFlush(
                IdempotencyKey.generate(idempotencyKey, requestHash, LocalDateTime.now().plus(TTL)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(String idempotencyKey) {
        idempotencyKeyRepository.deleteById(idempotencyKey);
    }
}
