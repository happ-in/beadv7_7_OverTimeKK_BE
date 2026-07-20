package com.programmers.kdt.order.entity;

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
        // 검증로직 추가 예정
        this.ticketId = ticketId;
        this.ticketPrice = ticketPrice;
    }

    public static OrderItem create(Long ticketId, Long ticketPrice){
        return new OrderItem(ticketId, ticketPrice);

    }

    public void assignOrder(Order order){
        this.order = order;
    }


}
