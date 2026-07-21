package com.programmers.kdt.payment.service;


import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.entity.Order;
import com.programmers.kdt.order.repository.OrderRepository;
import com.programmers.kdt.payment.client.PgApproveCommand;
import com.programmers.kdt.payment.client.PgApproveResult;
import com.programmers.kdt.payment.client.PgClient;
import com.programmers.kdt.payment.client.PgReadyResult;
import com.programmers.kdt.payment.dto.*;
import com.programmers.kdt.payment.entity.Payment;
import com.programmers.kdt.payment.entity.PaymentStatus;
import com.programmers.kdt.payment.exception.PaymentErrorCode;
import com.programmers.kdt.payment.repository.PaymentRefundRepository;
import com.programmers.kdt.payment.repository.PaymentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRefundRepository paymentRefundRepository;
    @Mock
    private PgClient pgClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, orderRepository, paymentRefundRepository, pgClient);
    }

    @Nested
    @DisplayName("결제 생성")
    class Pay {

        private final CreatePaymentRequest request = new CreatePaymentRequest(1L, 10000L);

        @Test
        @DisplayName("정상 요청이면 결제가 생성된다.")
        void successPayment() {
            Order order = Mockito.mock(Order.class);
            when(order.getOrderId()).thenReturn(1L);
            when(order.getUserId()).thenReturn(1L);
            when(order.getTotalAmount()).thenReturn(10000L);

            PgReadyResult readyResult = mock(PgReadyResult.class);
            when(readyResult.transactionKey()).thenReturn("PG_KEY_123");

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
            when(pgClient.ready(any())).thenReturn(readyResult);

            CreatePaymentResponse response = paymentService.pay(request);

            assertThat(response).isNotNull();
            verify(paymentRepository).save(any());
        }

        @Test
        @DisplayName("주문이 존재하지 않으면 예외가 발생한다.")
        void payOrderNotFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.pay(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.ORDER_NOT_FOUND);

            verifyNoInteractions(pgClient);
        }

        @Test
        @DisplayName("이미 결제가 생성된 주문이면 예외가 발생한다.")
        void payAlreadyExists() {
            Order order = Mockito.mock(Order.class);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(paymentRepository.existsByOrderId(1L)).thenReturn(true);

            assertThatThrownBy(() -> paymentService.pay(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);

            verifyNoInteractions(pgClient);
        }

        @Test
        @DisplayName("요청 금액이 주문 금액과 다르면 예외가 발생한다.")
        void payAmountMismatch() {
            Order order = Mockito.mock(Order.class);
            when(order.getTotalAmount()).thenReturn(15000L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(paymentRepository.existsByOrderId(1L)).thenReturn(false);

            assertThatThrownBy(() -> paymentService.pay(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);

            verifyNoInteractions(pgClient);

        }

        @Test
        @DisplayName("PG사 요청이 실패하면 예외가 발생하고 저장되지 않는다.")
        void payPgFailure() {
            Order order = Mockito.mock(Order.class);
            when(order.getTotalAmount()).thenReturn(10000L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(paymentRepository.existsByOrderId(1L)).thenReturn(false);
            when(pgClient.ready(any())).thenThrow(new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED));

            assertThatThrownBy(() -> paymentService.pay(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PG_REQUEST_FAILED);

            verify(paymentRepository, never()).save(any());

        }
    }

    @Nested
    @DisplayName("결제 승인")
    class Confirm {

        private Payment payment;

        @BeforeEach
        void setUp() {
            payment = Payment.create(1L, 100L, 10000L);
            payment.assignPaymentKey("PG_KEY_123");
        }

        @Test
        @DisplayName("PG 승인이 성공하면 결제 상태가 PAID로 바뀐다.")
        void confirmSuccess() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            PgApproveResult approveResult = mock(PgApproveResult.class);
            when(approveResult.success()).thenReturn(true);
            when(pgClient.approve(any())).thenReturn(approveResult);

            ConfirmPaymentResponse response = paymentService.confirm(1L, new ConfirmPaymentRequest("PG_KEY_123"));

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("PG 승인이 실패하면 결제 상태가 FAILED로 바뀐다.")
        void confirmFailure() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            PgApproveResult approveResult = mock(PgApproveResult.class);
            when(approveResult.success()).thenReturn(false);
            when(pgClient.approve(any())).thenReturn(approveResult);

            paymentService.confirm(1L, new ConfirmPaymentRequest("PG_KEY_123"));

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);

        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다.")
        void confirmPaymentNotFound() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.confirm(1L, new ConfirmPaymentRequest("PG_KEY_123")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);

            verifyNoInteractions(pgClient);

        }

        @Test
        @DisplayName("transactionKey가 일치하지 않으면 예외가 발생한다.")
        void confirmKeyMismatch() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            assertThatThrownBy(() -> paymentService.confirm(1L, new ConfirmPaymentRequest("WRONG_KEY")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PAYMENT_KEY_MISMATCH);

            verifyNoInteractions(pgClient);
        }

        @Test
        @DisplayName("READY 상태가 아니면 예외가 발생하고 PG 승인 요청은 나가지 않는다.")
        void confirmInvalidStatus() {
            payment.approve();

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.confirm(1L, new ConfirmPaymentRequest("PG_KEY_123")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS);

            verifyNoInteractions(pgClient);
        }

        @Test
        @DisplayName("PG 승인 요청이 실패하면 예외가 발생하고 상태는 변경되지 않는다.")
        void confirmPgRequestFailed() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            when(pgClient.approve(any())).thenThrow(new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED));

            assertThatThrownBy(() -> paymentService.confirm(1L, new ConfirmPaymentRequest("PG_KEY_123")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PG_REQUEST_FAILED);

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.READY);
        }

    }

    @Nested
    @DisplayName("결제 실패")
    class Fail {
        private Payment payment;

        @BeforeEach
        void setUp() {
            payment = Payment.create(1L, 100L, 10000L);
            payment.assignPaymentKey("PG_KEY_123");
        }

        @Test
        @DisplayName("READY 상태의 결제는 실패 처리되고 PG 취소 요청이 나간다.")
        void failSuccess() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            FailPaymentResponse response = paymentService.fail(1L, new FailPaymentRequest("고객 요청"));

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(response).isNotNull();
            verify(pgClient).cancel(any());
        }

        @Test
        @DisplayName("결제를 찾을 수 없을면 예외를 발생한다.")
        void failPaymentNotFound() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.fail(1L, new FailPaymentRequest("고객 요청")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);

            verifyNoInteractions(pgClient);
        }

        @Test
        @DisplayName("PAID 상태의 결제는 실패 처리할 수 없고 PG 요청도 나가지 않는다.")
        void failInvalidStatus() {
            payment.approve();

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.fail(1L, new FailPaymentRequest("고객 요청")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS);

            verifyNoInteractions(pgClient);
        }

        @Test
        @DisplayName("이미 FAILED된 결제에 다시 호출하면 중복 무시되고 PG 요청은 나가지 않는다.")
        void failAlreadyFailed() {
            payment.fail();

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

            FailPaymentResponse response = paymentService.fail(1L, new FailPaymentRequest("고객 요청"));

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(response).isNotNull();

            verifyNoInteractions(pgClient);

        }

        @Test
        @DisplayName("PG 취소 요청이 실패하면 예외가 발생한다.")
        void failPgRequestFailed() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
            doThrow(new BusinessException(PaymentErrorCode.PG_REQUEST_FAILED)).when(pgClient).cancel(any());

            assertThatThrownBy(() -> paymentService.fail(1L, new FailPaymentRequest("PG사 취소 요청")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(PaymentErrorCode.PG_REQUEST_FAILED);
        }

        @Nested
        @DisplayName("결제 내역 조회")
        class GetPaymentHistory {

            @Test
            @DisplayName("사용자의 결제 내역을 조회한다.")
            void getPaymentHistorySuccess() {
                Payment payment1 = Payment.create(1L, 100L, 10000L);
                Payment payment2 = Payment.create(2L, 100L, 20000L);
                Pageable pageable = PageRequest.of(0, 10);
                Page<Payment> paymentPage = new PageImpl<>(List.of(payment1, payment2), pageable, 2);

                when(paymentRepository.findByUserId(100L, pageable)).thenReturn(paymentPage);

                Page<GetPaymentHistoryResponse> result = paymentService.getPaymentHistory(100L, pageable);

                assertThat(result.getTotalElements()).isEqualTo(2);
                assertThat(result.getContent()).hasSize(2);
            }
        }

        @Test
        @DisplayName("결제 내역이 없으면 빈 페이지를 반환한다.")
        void getPaymentHistoryEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(paymentRepository.findByUserId(100L, pageable)).thenReturn(emptyPage);

            Page<GetPaymentHistoryResponse> result = paymentService.getPaymentHistory(100L, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}