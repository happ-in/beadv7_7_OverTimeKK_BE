package com.programmers.kdt.payment.repository;

import com.programmers.kdt.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByUserId(Long userId, Pageable pageable);

    boolean existsByOrderId(Long orderId);

    Optional<Payment> findByOrderId(Long orderId);
}
