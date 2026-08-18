package com.programmers.kdt.order.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.client.TicketClient;
import com.programmers.kdt.order.client.TicketInfo;
import com.programmers.kdt.order.dto.CancelOrderRequest;
import com.programmers.kdt.order.dto.CancelOrderResponse;
import com.programmers.kdt.order.dto.CreateOrderRequest;
import com.programmers.kdt.order.dto.CreateOrderResponse;
import com.programmers.kdt.order.dto.GetOrderHistoryResponse;
import com.programmers.kdt.order.dto.OrderTicketRequest;
import com.programmers.kdt.order.dto.TicketReserveRequest;
import com.programmers.kdt.order.dto.ValidateTicketRequest;
import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.entity.OrderItem;
import com.programmers.kdt.order.entity.OrderStatus;
import com.programmers.kdt.order.event.TicketCancelRequestEvent;
import com.programmers.kdt.order.event.TicketReleaseRequestEvent;
import com.programmers.kdt.order.exception.OrderErrorCode;
import com.programmers.kdt.order.repository.OrderRepository;
import com.programmers.kdt.payment.dto.RefundPaymentRequest;
import com.programmers.kdt.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long TICKET_ID = 100L;
    private static final Long PRICE = 50_000L;
    private static final String HOLD_KEY = "hold-key";

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private TicketClient ticketClient;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                ticketClient,
                paymentService,
                eventPublisher
        );
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("티켓을 검증하고 PENDING 상태의 주문을 저장한다")
        void success() {
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(10);
            CreateOrderRequest request = new CreateOrderRequest(
                    TICKET_ID, USER_ID, PRICE, expiredAt, HOLD_KEY
            );
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "orderId", ORDER_ID);
                return order;
            });

            CreateOrderResponse response = orderService.createOrder(request);

            assertThat(response.orderId()).isEqualTo(ORDER_ID);
            assertThat(response.orderStatus()).isEqualTo(OrderStatus.PENDING.name());

            ArgumentCaptor<ValidateTicketRequest> validateCaptor =
                    ArgumentCaptor.forClass(ValidateTicketRequest.class);
            verify(ticketClient).validateTicket(validateCaptor.capture());
            assertThat(validateCaptor.getValue()).isEqualTo(
                    new ValidateTicketRequest(TICKET_ID, USER_ID, PRICE, HOLD_KEY)
            );

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getUserId()).isEqualTo(USER_ID);
            assertThat(savedOrder.getTotalAmount()).isEqualTo(PRICE);
            assertThat(savedOrder.getExpiresAt()).isEqualTo(expiredAt);
            assertThat(savedOrder.getItems()).singleElement().satisfies(item -> {
                assertThat(item.getTicketId()).isEqualTo(TICKET_ID);
                assertThat(item.getHoldKey()).isEqualTo(HOLD_KEY);
            });
        }

        @Test
        @DisplayName("티켓 검증에 실패하면 주문을 저장하지 않는다")
        void ticketValidationFailure() {
            CreateOrderRequest request = new CreateOrderRequest(
                    TICKET_ID, USER_ID, PRICE, LocalDateTime.now().plusMinutes(10), HOLD_KEY
            );
            BusinessException exception = new BusinessException(OrderErrorCode.TICKET_VALIDATION_FAILED);
            org.mockito.Mockito.doThrow(exception)
                    .when(ticketClient).validateTicket(any(ValidateTicketRequest.class));

            assertThatThrownBy(() -> orderService.createOrder(request)).isSameAs(exception);

            verify(orderRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("결제 전 주문을 취소하면 좌석 해제 이벤트를 발행한다")
    void cancelPendingOrder() {
        Order order = pendingOrder();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        CancelOrderResponse response = orderService.cancelPendingOrder(ORDER_ID);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CANCELLED.name());
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        ArgumentCaptor<TicketReleaseRequestEvent> captor =
                ArgumentCaptor.forClass(TicketReleaseRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isEqualTo(
                new TicketReleaseRequestEvent(ORDER_ID, TICKET_ID, HOLD_KEY)
        );
    }

    @Nested
    @DisplayName("주문 완료")
    class CompleteOrder {

        @Test
        @DisplayName("결제 시작 주문의 티켓을 예약하고 주문을 완료한다")
        void success() {
            Order order = paymentStartedOrder();
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            orderService.completeOrder(ORDER_ID);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
            verify(ticketClient).reserveTicket(
                    new TicketReserveRequest(TICKET_ID, HOLD_KEY, USER_ID)
            );
        }

        @Test
        @DisplayName("이미 완료된 주문이면 티켓 예약을 다시 요청하지 않는다")
        void alreadyCompleted() {
            Order order = completedOrder();
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            orderService.completeOrder(ORDER_ID);

            verify(ticketClient, never()).reserveTicket(any());
        }
    }

    @Test
    @DisplayName("만료 대상 주문을 만료시키고 각각 좌석 해제 이벤트를 발행한다")
    void expireOrders() {
        Order first = pendingOrder(ORDER_ID, TICKET_ID, HOLD_KEY);
        Order second = pendingOrder(2L, 200L, "second-hold-key");
        when(orderRepository.findAllByOrderStatusAndExpiresAtLessThanEqual(
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING), any(LocalDateTime.class)
        )).thenReturn(List.of(first, second));

        orderService.expireOrders();

        assertThat(first.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
        assertThat(second.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
        ArgumentCaptor<TicketReleaseRequestEvent> captor =
                ArgumentCaptor.forClass(TicketReleaseRequestEvent.class);
        verify(eventPublisher, org.mockito.Mockito.times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues()).containsExactly(
                new TicketReleaseRequestEvent(ORDER_ID, TICKET_ID, HOLD_KEY),
                new TicketReleaseRequestEvent(2L, 200L, "second-hold-key")
        );
    }

    @Nested
    @DisplayName("결제 시작")
    class StartPayment {

        @Test
        @DisplayName("유효한 PENDING 주문을 PAYMENT_STARTED로 변경한다")
        void success() {
            Order order = pendingOrder();
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            orderService.startPayment(ORDER_ID);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_STARTED);
        }

        @Test
        @DisplayName("이미 만료 시각이 지난 주문이면 예외가 발생한다")
        void expiredOrder() {
            Order order = orderWithExpiration(LocalDateTime.now().minusSeconds(1));
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            assertBusinessException(
                    () -> orderService.startPayment(ORDER_ID),
                    OrderErrorCode.ORDER_ALREADY_EXPIRED
            );
        }
    }

    @Test
    @DisplayName("완료 주문을 환불하면 주문을 취소하고 티켓 취소 이벤트를 발행한다")
    void cancelCompletedOrder() {
        Order order = completedOrder();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        CancelOrderResponse response = orderService.cancelCompletedOrder(
                ORDER_ID, new CancelOrderRequest("단순 변심")
        );

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CANCELLED.name());
        verify(paymentService).refund(
                org.mockito.ArgumentMatchers.eq(ORDER_ID),
                org.mockito.ArgumentMatchers.argThat(request -> request.reason().equals("단순 변심"))
        );
        verify(eventPublisher).publishEvent(
                new TicketCancelRequestEvent(TICKET_ID, USER_ID, ORDER_ID)
        );
    }

    @Nested
    @DisplayName("주문 내역 조회")
    class GetOrderHistory {

        @Test
        @DisplayName("완료/취소/만료 주문과 티켓 정보를 조합해 최신 주문 내역을 반환한다")
        void success() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 8, 11, 12, 0);
            Order completed = completedOrder();
            Order cancelled = cancelledOrder();
            Order expired = expiredOrder();
            ReflectionTestUtils.setField(completed, "createdAt", createdAt);
            when(orderRepository.findByUserIdAndOrderStatusInOrderByCreatedAtDesc(
                    USER_ID, List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED, OrderStatus.EXPIRED)
            )).thenReturn(List.of(completed, cancelled, expired));
            when(ticketClient.getTickets(new OrderTicketRequest(List.of(TICKET_ID, 200L, 300L))))
                    .thenReturn(List.of(
                            new TicketInfo(TICKET_ID, "오버타임 콘서트", "VIP"),
                            new TicketInfo(200L, "오버타임 콘서트", "R"),
                            new TicketInfo(300L, "오버타임 콘서트", "A")
                    ));

            List<GetOrderHistoryResponse> responses = orderService.getOrderHistory(USER_ID);

            assertThat(responses).hasSize(3);
            assertThat(responses.getFirst()).satisfies(response -> {
                assertThat(response.orderId()).isEqualTo(ORDER_ID);
                assertThat(response.orderStatus()).isEqualTo(OrderStatus.COMPLETED.name());
                assertThat(response.performanceName()).isEqualTo("오버타임 콘서트");
                assertThat(response.orderedAt()).isEqualTo(createdAt);
                assertThat(response.zone()).isEqualTo("VIP");
                assertThat(response.quantity()).isEqualTo(1);
                assertThat(response.totalAmount()).isEqualTo(PRICE);
            });
            assertThat(responses).extracting(GetOrderHistoryResponse::orderStatus)
                    .containsExactly(
                            OrderStatus.COMPLETED.name(),
                            OrderStatus.CANCELLED.name(),
                            OrderStatus.EXPIRED.name()
                    );
        }

        @Test
        @DisplayName("조회 대상 주문이 없으면 티켓 정보를 조회하지 않고 빈 목록을 반환한다")
        void empty() {
            when(orderRepository.findByUserIdAndOrderStatusInOrderByCreatedAtDesc(
                    USER_ID, List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED, OrderStatus.EXPIRED)
            )).thenReturn(List.of());

            assertThat(orderService.getOrderHistory(USER_ID)).isEmpty();

            verifyNoInteractions(ticketClient);
        }

        @Test
        @DisplayName("주문에 대응하는 티켓 정보가 없으면 예외가 발생한다")
        void ticketInfoNotFound() {
            Order order = completedOrder();
            when(orderRepository.findByUserIdAndOrderStatusInOrderByCreatedAtDesc(
                    USER_ID, List.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED, OrderStatus.EXPIRED)
            )).thenReturn(List.of(order));
            when(ticketClient.getTickets(new OrderTicketRequest(List.of(TICKET_ID)))).thenReturn(List.of());

            assertBusinessException(
                    () -> orderService.getOrderHistory(USER_ID),
                    OrderErrorCode.TICKET_INFO_NOT_FOUND
            );
        }
    }

    @Test
    @DisplayName("결제 실패를 처리하면 PAYMENT_STARTED 주문이 PENDING으로 돌아간다")
    void handlePaymentFailed() {
        Order order = paymentStartedOrder();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.handlePaymentFailed(ORDER_ID);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다")
    void orderNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertBusinessException(
                () -> orderService.cancelPendingOrder(ORDER_ID),
                OrderErrorCode.ORDER_NOT_FOUND
        );
        verifyNoInteractions(eventPublisher);
    }

    private Order pendingOrder() {
        return pendingOrder(ORDER_ID, TICKET_ID, HOLD_KEY);
    }

    private Order pendingOrder(Long orderId, Long ticketId, String holdKey) {
        Order order = Order.create(
                USER_ID,
                List.of(OrderItem.create(ticketId, PRICE, holdKey)),
                LocalDateTime.now().plusMinutes(10)
        );
        ReflectionTestUtils.setField(order, "orderId", orderId);
        return order;
    }

    private Order orderWithExpiration(LocalDateTime expiredAt) {
        Order order = Order.create(
                USER_ID,
                List.of(OrderItem.create(TICKET_ID, PRICE, HOLD_KEY)),
                expiredAt
        );
        ReflectionTestUtils.setField(order, "orderId", ORDER_ID);
        return order;
    }

    private Order paymentStartedOrder() {
        Order order = pendingOrder();
        order.startPayment(LocalDateTime.now());
        return order;
    }

    private Order completedOrder() {
        Order order = paymentStartedOrder();
        order.complete();
        return order;
    }

    private Order cancelledOrder() {
        Order order = pendingOrder(2L, 200L, "cancel-hold-key");
        order.cancelPending();
        return order;
    }

    private Order expiredOrder() {
        Order order = pendingOrder(3L, 300L, "expire-hold-key");
        order.expire();
        return order;
    }

    private void assertBusinessException(Runnable action, OrderErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }
}
