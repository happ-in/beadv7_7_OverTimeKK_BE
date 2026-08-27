package com.programmers.kdt.payment.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.payment.entity.key.IdempotencyKey;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.repository.IdempotencyKeyRepository;
import com.programmers.kdt.payment.service.tx.IdempotencyKeyTxOps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({IdempotencyKeyServiceImpl.class, IdempotencyKeyTxOps.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class IdempotencyKeyServiceIntegrationTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @Autowired
    private IdempotencyKeyServiceImpl idempotencyKeyService;

    @AfterEach
    void tearDown() {
        idempotencyKeyRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 키로 동시에 두 번 요청이 들어오면, 두 번째 요청은 DB 유니크 제약으로 걸려서 PAYMENT_ALREADY_EXISTS를 받는다.")
    void concurrentDuplicateRequest_secondCallBlockedByUniqueConstraint() {
        Optional<String> first = idempotencyKeyService.generate("dup-key", "same-hash");
        assertThat(first).isEmpty();

        assertThatThrownBy(() -> idempotencyKeyService.generate("dup-key", "same-hash"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("완료된 요청을 같은 키로 재요청하면 실제 DB에 저장된 응답을 그대로 캐시로 반환한다.")
    void completedRequest_replayReturnsRealPersistedResponse() {
        idempotencyKeyService.generate("replay-key", "same-hash");
        idempotencyKeyService.complete("replay-key", "{\"paymentId\":42}");

        Optional<String> result = idempotencyKeyService.generate("replay-key", "same-hash");

        assertThat(result).contains("{\"paymentId\":42}");
    }

    @Test
    @DisplayName("같은 키로 다른 내용의 요청이 오면 IDEMPOTENCY_KEY_CONFLICT를 받는다.")
    void sameKeyDifferentPayload_realConflict() {
        idempotencyKeyService.generate("conflict-key", "hash-A");

        assertThatThrownBy(() -> idempotencyKeyService.generate("conflict-key", "hash-B"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PaymentErrorCode.IDEMPOTENCY_KEY_CONFLICT);
    }

    @Test
    @DisplayName("TTL 만료된 키는 재요청 시 실제로 삭제 후 재생성된다.")
    void expiredKey_realExpireAndReinsert() {
        IdempotencyKey expired = idempotencyKeyRepository.saveAndFlush(
                IdempotencyKey.generate("expired-key", "hash-old", LocalDateTime.now().minusSeconds(1)));

        Optional<String> result = idempotencyKeyService.generate("expired-key", "hash-new");

        assertThat(result).isEmpty();
        IdempotencyKey reinserted = idempotencyKeyRepository.findById("expired-key").orElseThrow();
        assertThat(reinserted.getRequestHash()).isEqualTo("hash-new");
        assertThat(reinserted.getResponseBody()).isNull();
    }


}
