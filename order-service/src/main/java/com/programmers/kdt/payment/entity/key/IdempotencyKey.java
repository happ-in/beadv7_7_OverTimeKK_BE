package com.programmers.kdt.payment.entity.key;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_key")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class IdempotencyKey implements Persistable<String> {

    // 클라이언트에서 전달해준 멱등키, UUID, 길이 300
    @Id
    @Column(nullable = false, length = 300)
    private String idempotencyKey;

    @Column(nullable = false)
    private String requestHash;

    @Lob
    private String responseBody;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Transient
    private boolean isNew = true;

    public static IdempotencyKey generate(String idempotencyKey, String requestHash, LocalDateTime expiresAt) {
        IdempotencyKey key = new IdempotencyKey();
        key.idempotencyKey = idempotencyKey;
        key.requestHash = requestHash;
        key.expiresAt = expiresAt;
        return key;
    }

    public void complete(String responseBody) {
        this.responseBody = responseBody;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    @Override
    public @Nullable String getId() {
        return idempotencyKey;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
