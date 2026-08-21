package com.programmers.kdt.payment.repository;

import com.programmers.kdt.payment.entity.key.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

}
