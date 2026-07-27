package com.programmers.kdt.payment.repository;

import com.programmers.kdt.order.entity.OrderItem;
import com.programmers.kdt.payment.dto.PointEarnTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface PointEarnTargetRepository extends JpaRepository<OrderItem, Long> {

    // REST로 받아온 ticketID 목록으로 order_items, orders 조회
    @Query("""
    SELECT new com.programmers.kdt.payment.dto.PointEarnTarget(o.userId, oi.ticketId, oi.ticketPrice)
    FROM OrderItem oi
    Join oi.order o
    WHERE oi.ticketId IN :ticketIds
    AND o.orderStatus = com.programmers.kdt.order.entity.OrderStatus.COMPLETED
    """)
    List<PointEarnTarget> findEarnTargetsByTicketIds(@Param("ticketIds") List<Long> ticketIds);
}
