package com.programmers.kdt.order.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private LocalDateTime cancelledAt;


    private Order(Long userId, List<OrderItem> items){
        validateItems(items);
        this.userId = userId;
        this.orderStatus = OrderStatus.PENDING;
        this.orderDate = LocalDate.now();
        for(OrderItem item : items){
            assignOrderItem(item);
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
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }
        if(items.stream().anyMatch(item -> item==null)){
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }
    }

    // 주문항목 추가
    private void assignOrderItem(OrderItem item){
        this.items.add(item);
        item.assignOrder(this);
    }

    // 총 주문 금액 계산
    private Long calculateAmount(){
        return items.stream()
                .mapToLong(OrderItem::getTicketPrice)
                .sum();
    }


    // 주문 완료 PENDING -> COMPLETED
    public void complete(){
        if(orderStatus == OrderStatus.COMPLETED){
            return; // 이미 주문 완료된 상태, 중복 무시
        }
        if(orderStatus != OrderStatus.PENDING){
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PENDING);
        }
        this.orderStatus = OrderStatus.COMPLETED;
    }

    // 주문 만료 PENDING -> EXPIRED
    public void expire(){
        if(orderStatus == OrderStatus.EXPIRED){
            return; // 이미 주문 만료된 상태, 중복 무시
        }
        if(orderStatus != OrderStatus.PENDING){
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PENDING);
        }
        this.orderStatus = OrderStatus.EXPIRED;
    }

    // 취소 가능한 주문인지 검증
    public void validateCancel(){
        if(orderStatus == OrderStatus.CANCELLED){
            throw new BusinessException(OrderErrorCode.ORDER_ALREADY_CANCEL);
        }
        if(orderStatus != OrderStatus.COMPLETED){
            throw new BusinessException(OrderErrorCode.ORDER_NOT_COMPLETED);
        }
    }

    // 주문 취소 COMPLETED -> CANCELED
    public void cancel(){
        validateCancel();
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    // 티켓 ID
    public Long getTicketId(){
        return items.getFirst().getTicketId();
    }

}
