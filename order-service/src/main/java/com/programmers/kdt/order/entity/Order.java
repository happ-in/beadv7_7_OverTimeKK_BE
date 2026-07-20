package com.programmers.kdt.order.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private LocalDate orderDate;


    private Order(Long userId, List<OrderItem> items){
        validateItems(items);
        this.userId = userId;
        this.orderStatus = OrderStatus.PENDING;
        this.orderDate = LocalDate.now();

        for(OrderItem item : items){
            addOrderItem(item);
        }
        this.totalAmount = calculateAmount();
    }

    // 주문 생성
    public static Order create(Long userId, List<OrderItem> items){
        return new Order(userId, items);
    }

    // 주문 검증
    private void validateItems(List<OrderItem> items){
        if(items == null || items.isEmpty()){
            throw new IllegalStateException("주문할 상품이 없습니다.");
        }
    }

    // 주문항목 추가
    public void addOrderItem(OrderItem item){
        this.items.add(item);
        // 주문과 주문항목 연결
        if(item.getOrder() != this){
            item.assignOrder(this);
        }
    }

    // 총 주문 금액 계산
    private Long calculateAmount(){
        return items.stream()
                .mapToLong(OrderItem::getTicketPrice)
                .sum();
    }


    // 주문 완료 PENDING -> COMPLETED
    public void complete(){
        if(orderStatus != OrderStatus.PENDING){
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PENDING);
        }
        this.orderStatus = OrderStatus.COMPLETED;
    }

    // 주문 만료 PENDING -> EXPIRED
    public void expire(){
        if(orderStatus != OrderStatus.PENDING){
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PENDING);
        }
        this.orderStatus = OrderStatus.EXPIRED;
    }

    // 주문 취소 COMPLETED -> CANCELED
    public void cancel(){
        if(orderStatus != OrderStatus.COMPLETED){
            throw new BusinessException(OrderErrorCode.ORDER_NOT_COMPLETED);
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

}
