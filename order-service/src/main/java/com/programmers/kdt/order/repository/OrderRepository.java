package com.programmers.kdt.order.repository;

import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findAllByOrderStatusAndExpiresAtLessThanEqual(
            OrderStatus orderStatus,
            LocalDateTime expiresAt
    );
}
