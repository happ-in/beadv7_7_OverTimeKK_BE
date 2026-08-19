package com.programmers.kdt.order.repository;

import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderStatusAndExpiresAtLessThanEqual(
            OrderStatus orderStatus,
            LocalDateTime expiresAt
    );

    List<Order> findByUserIdAndOrderStatusInOrderByCreatedAtDesc(
            Long userId,
            Collection<OrderStatus> orderStatuses
    );

    @Query("""
        select distinct o
        from Order o
        join fetch o.items item
        where item.ticketId in :ticketIds
        """)
    List<Order> findAllByTicketIds(
            @Param("ticketIds") List<Long> ticketIds
    );
}
