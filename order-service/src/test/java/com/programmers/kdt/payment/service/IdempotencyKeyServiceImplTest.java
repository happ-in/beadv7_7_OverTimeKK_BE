package com.programmers.kdt.payment.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.payment.entity.key.IdempotencyKey;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.repository.IdempotencyKeyRepository;
import com.programmers.kdt.payment.service.tx.IdempotencyKeyTxOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class IdempotencyKeyServiceImplTest {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock
    private IdempotencyKeyTxOps txOps;
    @InjectMocks
    private IdempotencyKeyServiceImpl idempotencyKeyService;

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("최초 요청이면 새로 저장하고 빈 값을 반환한다.")
        void freshKey_insertsAndReturnEmpty() {
            Optional<String> result = idempotencyKeyService.generate("key-1", "hash-1");

            assertThat(result).isEmpty();
            verify(txOps).tryInsert("key-1", "hash-1");
        }

        @Test
        @DisplayName("같은 키+같은 요청이 이미 완료됐으면 캐시된 응답을 그대로 반환한다.")
        void sameKeyHash_completed_returnsCachedResponse() {
            doThrow(new DataIntegrityViolationException("duplicate key"))
                    .when(txOps).tryInsert("key-1", "hash-1");

            IdempotencyKey existing = IdempotencyKey.generate("key-1", "hash-1", LocalDateTime.now().plusMinutes(5));
            existing.complete("{\"paymentId\":1}");
            when(txOps.findFresh("key-1")).thenReturn(Optional.of(existing));

            Optional<String> result = idempotencyKeyService.generate("key-1", "hash-1");

            assertThat(result).contains("{\"paymentId\":1}");
            verify(txOps, never()).expireAndReinsert(any(), any(), any());
        }

        @Test
        @DisplayName("같은 키 + 같은 요청이 아직 처리중이면 PAYMENT_ALREADY_EXISTS 예외가 발생한다.")
        void sameKeySameHash_stillPending_throwsAlreadyExists() {
            doThrow(new DataIntegrityViolationException("dup"))
                    .when(txOps).tryInsert("key-1", "hash-1");

            IdempotencyKey pending = IdempotencyKey.generate("key-1", "hash-1", LocalDateTime.now().plusMinutes(5));
            when(txOps.findFresh("key-1")).thenReturn(Optional.of(pending));

            assertThatThrownBy(() -> idempotencyKeyService.generate("key-1", "hash-1"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("같은 키인데 요청 내용(hash)이 다르면 IDEMPOTENCY_KEY_CONFLICT 예외가 발생한다.")
        void sameKeyDifferentHash_throwsConflict() {
            doThrow(new DataIntegrityViolationException("dup"))
                    .when(txOps).tryInsert("key-1", "hash-2");

            IdempotencyKey existing = IdempotencyKey.generate("key-1", "hash-1", LocalDateTime.now().plusMinutes(5));
            when(txOps.findFresh("key-1")).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> idempotencyKeyService.generate("key-1", "hash-2"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }

        @Test
        @DisplayName("같은 키+다른 hash면, 기존 요청이 처리중이어도 CONFLICT가 ALREADY_EXISTS보다 우선한다.")
        void sameKeyDifferentHash_evenIfPending_conflictWinsOverAlreadyExists() {
            doThrow(new DataIntegrityViolationException("dup"))
                    .when(txOps).tryInsert("key-1", "hash-2");

            IdempotencyKey pendingDifferentHash = IdempotencyKey.generate("key-1", "hash-1", LocalDateTime.now().plusMinutes(5));
            when(txOps.findFresh("key-1")).thenReturn(Optional.of(pendingDifferentHash));

            assertThatThrownBy(() -> idempotencyKeyService.generate("key-1", "hash-2"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }

        @Test
        @DisplayName("기존 키가 만료됐으면 삭제 후 재삽입하고 빈 값을 반환한다.")
        void expiredKey_expiresAndReinserts() {
            doThrow(new DataIntegrityViolationException("dup"))
                    .when(txOps).tryInsert("key-1", "hash-1");

            IdempotencyKey expired = IdempotencyKey.generate("key-1", "hash-1", LocalDateTime.now().minusMinutes(1));
            when(txOps.findFresh("key-1")).thenReturn(Optional.of(expired));

            Optional<String> result = idempotencyKeyService.generate("key-1", "hash-1");

            assertThat(result).isEmpty();
            verify(txOps).expireAndReinsert("key-1", expired, "hash-1");
        }

        @Test
        @DisplayName("insert 충돌 후 재조회에서도 못 찾으면(찰나의 delete 경합) 원래 예외를 그대로 던진다.")
        void raceLoser_findFreshEmpty_rethrowsOriginalException() {
            DataIntegrityViolationException original = new DataIntegrityViolationException("dup");
            doThrow(original).when(txOps).tryInsert("key-1", "hash-1");

            assertThatThrownBy(() -> idempotencyKeyService.generate("key-1", "hash-1"))
                    .isSameAs(original);
        }
    }

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("존재하는 키의 응답 본문을 완료 처리한다.")
        void completeExistingKey() {
            IdempotencyKey key = IdempotencyKey.generate("key-1", "hash-1", LocalDateTime.now().plusMinutes(5));

            when(idempotencyKeyRepository.findById("key-1")).thenReturn(Optional.of(key));

            idempotencyKeyService.complete("key-1", "{\"result\":true}");

            assertThat(key.getResponseBody()).isEqualTo("{\"result\":true}");
        }

        @Test
        @DisplayName("존재하지 않는 키면 예외 없이 조용히 무시한다.")
        void missingKey_noOp() {
            when(idempotencyKeyRepository.findById("key-1")).thenReturn(Optional.empty());

            assertThatCode(() -> idempotencyKeyService.complete("key-1", "{}"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("release")
    class Release {
        @Test
        @DisplayName("키를 삭제한다.")
        void deletesKey() {
            idempotencyKeyService.release("key-1");

            verify(txOps).delete("key-1");
        }
    }
}