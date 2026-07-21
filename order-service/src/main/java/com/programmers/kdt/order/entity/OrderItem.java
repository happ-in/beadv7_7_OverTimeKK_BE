package com.programmers.kdt.order.entity;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private Long ticketPrice;

    private OrderItem(Long ticketId, Long ticketPrice){
        validateTicketPrice(ticketPrice);
        this.ticketId = ticketId;
        this.ticketPrice = ticketPrice;
    }

    public static OrderItem create(Long ticketId, Long ticketPrice){
        return new OrderItem(ticketId, ticketPrice);

    }

    private void validateTicketPrice(Long ticketPrice){
        if(ticketPrice == null || ticketPrice <=0){
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_AMOUNT);
        }
    }

    void assignOrder(Order order){
        this.order = order;
    }

}
