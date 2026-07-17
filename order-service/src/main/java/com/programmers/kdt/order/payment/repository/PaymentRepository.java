package com.programmers.kdt.order.payment.repository;

import com.programmers.kdt.order.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
