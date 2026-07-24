package com.programmers.kdt.order.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.client.TicketClient;
import com.programmers.kdt.order.client.TicketHoldResult;
import com.programmers.kdt.order.client.TicketInfo;
import com.programmers.kdt.order.dto.*;
import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.entity.OrderItem;
import com.programmers.kdt.order.entity.OrderStatus;
import com.programmers.kdt.order.event.OrderCancelledEvent;
import com.programmers.kdt.order.exception.OrderErrorCode;
import com.programmers.kdt.order.repository.OrderRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketClient ticketClient;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OrderServiceImpl orderServiceImpl;

    @BeforeEach
    void setUp(){
        orderServiceImpl = new OrderServiceImpl(orderRepository, ticketClient, paymentService, eventPublisher);
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrde{

        private final CreateOrderRequest request = new CreateOrderRequest(1L, 10L);

        @Test
        @DisplayName("티켓을 점유하고 올바른 주문을 생성한다")
        void createOrderSuccess() {
            // given
            LocalDateTime holdExpiresAt = LocalDateTime.now().plusMinutes(10);
            TicketHoldResult holdTicket =
                    new TicketHoldResult(10L, 50_000L, holdExpiresAt);

            when(ticketClient.holdSeat(10L, 1L))
                    .thenReturn(holdTicket);

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            CreateOrderResponse response =
                    orderServiceImpl.createOrder(request);

            // then
            ArgumentCaptor<Order> captor =
                    ArgumentCaptor.forClass(Order.class);

            verify(orderRepository).save(captor.capture());

            Order savedOrder = captor.getValue();

            assertThat(savedOrder.getUserId()).isEqualTo(1L);
            assertThat(savedOrder.getTicketId()).isEqualTo(10L);
            assertThat(savedOrder.getTotalAmount()).isEqualTo(50_000L);
            assertThat(savedOrder.getOrderStatus())
                    .isEqualTo(OrderStatus.PENDING);

            assertThat(response.orderStatus())
                    .isEqualTo(OrderStatus.PENDING.name());
        }

        @Test
        @DisplayName("티켓 점유 요청이 실패하면 주문을 저장하지 않는다.")
        void createOrderFailure(){
            // given
            RuntimeException exception = new RuntimeException("좌석 점유 실패");

            when(ticketClient.holdSeat(10L, 1L))
                    .thenThrow(exception);
            // when & then
            assertThatThrownBy(
                    () -> orderServiceImpl.createOrder(request)
            )
                    .isSameAs(exception);
            verify(orderRepository, never())
                    .save(any(Order.class));

        }
    }

    @Nested
    @DisplayName("주문 완료")
    class CompleteOrder{
        @Test
        @DisplayName("존재하는 주문은 완료 처리된다")
        void completeOrderSucess(){
            // given
            Long orderId = 10L;
            Order order = mock(Order.class);
            when(orderRepository.findById(orderId))
                    .thenReturn(Optional.of(order));

            // when
            orderServiceImpl.completeOrder(orderId);

            // then
            verify(order).complete();
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 예외가 발생한다.")
        void completeOrderNotFound(){
            // given
            Long orderId = 10L;

            Order order = mock(Order.class);
            when(orderRepository.findById(orderId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> orderServiceImpl.completeOrder(orderId)
            ).isInstanceOfSatisfying(
                    BusinessException.class,
                    exception ->{
                        assertThat(exception.getErrorCode())
                                .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
                    }
            );

        }
    }

    @Nested
    @DisplayName("주문 만료")
    class ExpireOrdes{

        @Test
        @DisplayName("10분이 지난 결제 대기 주문을 모두 만료 처린한다")
        void expireOrdersSuccess(){
            //given
            Order firstOrder = mock(Order.class);
            Order secondOrder = mock(Order.class);

            when(
                    orderRepository.findAllByOrderStatusAndExpiresAtLessThanEqual(
                            eq(OrderStatus.PENDING),
                            any(LocalDateTime.class)
                    )
            ).thenReturn(List.of(firstOrder, secondOrder));

            // when
            orderServiceImpl.expireOrders();

            // then
            verify(firstOrder).expire();
            verify(secondOrder).expire();

            verify(orderRepository)
                    .findAllByOrderStatusAndExpiresAtLessThanEqual(
                            eq(OrderStatus.PENDING),
                            any(LocalDateTime.class)
                    );
        }

        @Test
        @DisplayName("만료 대상 주문이 없으면 아무 주문도 변경하지 않는다")
        void expireOrdersEmpty(){
            // given
            when(orderRepository.findAllByOrderStatusAndExpiresAtLessThanEqual(
                    eq(OrderStatus.PENDING),
                    any(LocalDateTime.class)
            )).thenReturn(List.of());

            // when
            orderServiceImpl.expireOrders();

            // then
            verify(orderRepository)
                    .findAllByOrderStatusAndExpiresAtLessThanEqual(
                            eq(OrderStatus.PENDING),
                            any(LocalDateTime.class)
                    );

            verifyNoInteractions(
                    ticketClient,
                    paymentService,
                    eventPublisher
            );
        }

    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrder{

        CancelOrderRequest request = new CancelOrderRequest("사용자 요청");

        @Test
        @DisplayName("정상 요청이면 결제를 취소하고 주문을 취소한다")
        void cancelOrderSuccess(){
            // given
            Order order = mock(Order.class);
            when(orderRepository.findById(100L))
                    .thenReturn(Optional.of(order));

            when(order.getOrderId()).thenReturn(100L);
            when(order.getTicketId()).thenReturn(10L);
            when(order.getOrderStatus()).thenReturn(OrderStatus.CANCELLED);

            // when
            CancelOrderResponse response = orderServiceImpl.cancelOrder(100L, request);

            // then
            assertThat(response).isNotNull();
            verify(order).validateCancel();
            verify(paymentService).refund(
                    eq(100L),
                    argThat(
                            cancelRequest ->
                                    cancelRequest.reason()
                                    .equals("사용자 요청")
                    )
            );
            verify(order).cancel();
            verify(eventPublisher).publishEvent(
                    any(OrderCancelledEvent.class)
            );
        }

        @Test
        @DisplayName("취소할 수 없는 주문이면 결제 요청을 하지 않는다")
        void cancelOrderInvalidStatus(){
            // given
            Order order = mock(Order.class);

            RuntimeException exception =
                    new RuntimeException("취소할 수 없는 주문");

            when(orderRepository.findById(100L))
                    .thenReturn(Optional.of(order));

            doThrow(exception)
                    .when(order)
                    .validateCancel();

            // when & then
            assertThatThrownBy(
                    () -> orderServiceImpl.cancelOrder(100L, request)
            )
                    .isSameAs(exception);
            verify(paymentService, never()).refund(any(), any());
            verify(order, never()).cancel();
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("결제 취소 실패 시 주문 상태를 CANCELED로 변경하지 않는다")
        void cancelPaymentFailure() {
            // given
            Order order = mock(Order.class);

            RuntimeException exception =
                    new RuntimeException("결제 취소 실패");

            when(orderRepository.findById(100L))
                    .thenReturn(Optional.of(order));

            when(order.getOrderId()).thenReturn(100L);

            doThrow(exception)
                    .when(paymentService)
                    .refund(
                            eq(100L),
                            any()
                    );

            // when & then
            assertThatThrownBy(
                    () -> orderServiceImpl.cancelOrder(100L, request)
            )
                    .isSameAs(exception);

            verify(order).validateCancel();

            verify(order, never()).cancel();

            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("주문 취소 후 좌석 점유 해제 이벤트를 발행한다")
        void publishCancellationEvent() {
            // given
            Order order = mock(Order.class);

            when(orderRepository.findById(100L))
                    .thenReturn(Optional.of(order));

            when(order.getOrderId()).thenReturn(100L);
            when(order.getTicketId()).thenReturn(10L);
            when(order.getOrderStatus())
                    .thenReturn(OrderStatus.CANCELLED);

            // when
            orderServiceImpl.cancelOrder(100L, request);

            // then
            verify(eventPublisher).publishEvent(
                    any(OrderCancelledEvent.class)
            );

        }

    }

    @Nested
    @DisplayName("주문 내역 조회")
    class GetOrderHistory{
        @Test
        @DisplayName("사용자의 주문 내역과 티켓 정보를 조회한다")
        void getOrderHistorySuccess() {
            // given
            Long userId = 1L;
            LocalDateTime orderedAt =
                    LocalDateTime.of(2026, 7, 22, 15, 0);

            Order order = mock(Order.class);
            OrderItem orderItem = mock(OrderItem.class);
            TicketInfo ticketInfo = mock(TicketInfo.class);

            when(orderRepository.findByUserId(userId))
                    .thenReturn(List.of(order));

            when(order.getTicketId()).thenReturn(10L);

            when(ticketClient.getTicket(10L))
                    .thenReturn(ticketInfo);

            // GetOrderHistoryResponse.from()에서 사용하는 값
            when(order.getOrderId()).thenReturn(100L);
            when(order.getCreatedAt()).thenReturn(orderedAt);
            when(order.getItems()).thenReturn(List.of(orderItem));
            when(order.getTotalAmount()).thenReturn(50_000L);

            when(ticketInfo.performanceName()).thenReturn("오페라의 유령");
            when(ticketInfo.zone()).thenReturn("R석");

            // when
            List<GetOrderHistoryResponse> responses =
                    orderServiceImpl.getOrderHistory(userId);

            // then
            assertThat(responses).hasSize(1);

            GetOrderHistoryResponse response = responses.get(0);

            assertThat(response.orderId()).isEqualTo(100L);
            assertThat(response.performanceName()).isEqualTo("오페라의 유령");
            assertThat(response.orderdAt()).isEqualTo(orderedAt);
            assertThat(response.zone()).isEqualTo("R석");
            assertThat(response.quantity()).isEqualTo(1);
            assertThat(response.totalAmount()).isEqualTo(50_000L);

            verify(orderRepository).findByUserId(userId);
            verify(ticketClient).getTicket(10L);
        }

        @Test
        @DisplayName("주문 내역이 없으면 빈 목록을 반환한다")
        void getOrderHistoryEmpty() {
            // given
            when(orderRepository.findByUserId(1L))
                    .thenReturn(List.of());

            // when
            List<GetOrderHistoryResponse> responses =
                    orderServiceImpl.getOrderHistory(1L);

            // then
            assertThat(responses).isEmpty();

            verify(orderRepository).findByUserId(1L);
            verifyNoInteractions(ticketClient);
        }
    }

}
