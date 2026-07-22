package com.programmers.kdt.order.repository;

import com.programmers.kdt.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("select oi from OrderItem oi where oi.order.orderId = :orderId")
    Optional<OrderItem> findFirstByOrderId(@Param("orderId") Long orderId);
}
