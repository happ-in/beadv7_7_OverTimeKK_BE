package com.programmers.kdt.order.entity;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {

    private final LocalDateTime expiresAt =
            LocalDateTime.now().plusMinutes(10);

    private List<OrderItem> createItems(){
        return List.of(
                OrderItem.create(10L, 50_000L),
                OrderItem.create(20L, 70_000L),
                OrderItem.create(30L, 100_000L)
        );
    }

    @Nested
    @DisplayName("주문 생성")
    public class CreateOrder{

        @Test
        @DisplayName("정상적인 userId와 orderItem으로 생성하면 PENDING 상태로 초기화 된다.")
        void createOrder(){
            // given
            Long userId = 1L;
            List<OrderItem> items = createItems();

            // when
            Order order = Order.create(userId, items, expiresAt);

            // then
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotalAmount()).isEqualTo(220_000L);
        }

        @Test
        @DisplayName("주문 항목 목록이 null이면 예외가 발생한다.")
        void createOrderNullOrderItem(){
            assertThatThrownBy(() -> Order.create(1L, null, expiresAt))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception ->{
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(OrderErrorCode.ORDER_ITEMS_REQUIRED);
                            }
                    );
        }

        @Test
        @DisplayName("주문 항목 목록이 비어있으면 예외가 발생한다.")
        void createOrderEmptyOrderItem(){
            assertThatThrownBy(() -> Order.create(1L, List.of(), expiresAt))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(OrderErrorCode.ORDER_ITEMS_REQUIRED);
                            }
                    );

        }

        @Test
        @DisplayName("주문 항목 목록에 null 항목이 포함되면 예외가 발생한다.")
        void createOrderWithNullItem() {
            // given
            List<OrderItem> items = new ArrayList<>();
            items.add(null);

            // when & then
            assertThatThrownBy(() -> Order.create(1L, items, expiresAt))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(OrderErrorCode.ORDER_ITEMS_REQUIRED);
                            }
                    );
        }
    }

    @Nested
    @DisplayName("주문 완료")
    class CompleteOrder{

        @Test
        @DisplayName("PAYMENT_STARTED 상태에서 주문 완료되면 COMPLETED 상태로 전이된다.")
        void completeOrder() {
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.startPayment(expiresAt.minusSeconds(1));

            // when
            order.complete();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("이미 COMPLETED 상태면 예외 없이 무시해도 된다.")
        void ignoreAlreadyCompleted(){
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.startPayment(expiresAt.minusSeconds(1));
            order.complete();

            // when & then
            order.complete();

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("EXPIRED 상태에서 완료하면 예외가 발생한다.")
        void failToCompleteExpiredOrder(){
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.expire();

            // when & then
            assertThatThrownBy(order::complete)
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(OrderErrorCode.ORDER_NOT_PAYMENT_STARTED);
                            });

        }
    }

    @Nested
    @DisplayName("주문 만료")
    class ExpireOrder{

        @Test
        @DisplayName("PENDING 상태에서 만료 시간이 지나면 EXPIRED로 상태가 전이된다.")
        void expireOrder(){
            // given
            Order order = Order.create(1L, createItems(), expiresAt);

            // when
            order.expire();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
        }

        @Test
        @DisplayName("이미 EXPIRED 상태면 예외 없이 무시해도 된다.")
        void ignoreAlreadyExpired(){
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.expire();

            // when
            order.expire();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 만료하면 예외가 발생한다.")
        void failToExpireCompletedOrder() {
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.startPayment(expiresAt.minusSeconds(1));
            order.complete();

            // when & then
            assertThatThrownBy(order::expire)
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(OrderErrorCode.ORDER_NOT_PENDING)
                    );
        }

        @Test
        @DisplayName("CANCELLED 상태에서 만료하면 예외가 발생한다.")
        void failToExpireCancelledOrder() {
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.startPayment(expiresAt.minusSeconds(1));
            order.complete();
            order.cancel();

            // when & then
            assertThatThrownBy(order::expire)
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(OrderErrorCode.ORDER_NOT_PENDING)
                    );
        }

    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrder {

        @Test
        @DisplayName("COMPLETED 상태에서 취소하면 CANCELLED 상태로 전이된다.")
        void cancelOrder() {
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.startPayment(expiresAt.minusSeconds(1));
            order.complete();

            // when
            order.cancel();

            // then
            assertThat(order.getOrderStatus())
                    .isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("이미 CANCELLED 상태에서 다시 취소하면 예외가 발생한다.")
        void failToCancelAlreadyCancelled() {
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.startPayment(expiresAt.minusSeconds(1));
            order.complete();
            order.cancel();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(OrderErrorCode.ORDER_ALREADY_CANCELED)
                    );
        }

        @Test
        @DisplayName("PENDING 상태에서 취소하면 예외가 발생한다.")
        void failToCancelPendingOrder() {
            // given
            Order order = Order.create(1L, createItems(), expiresAt);

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(OrderErrorCode.ORDER_NOT_COMPLETED)
                    );
        }

        @Test
        @DisplayName("EXPIRED 상태에서 취소하면 예외가 발생한다.")
        void failToCancelExpiredOrder() {
            // given
            Order order = Order.create(1L, createItems(), expiresAt);
            order.expire();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(OrderErrorCode.ORDER_NOT_COMPLETED)
                    );
        }
    }
}