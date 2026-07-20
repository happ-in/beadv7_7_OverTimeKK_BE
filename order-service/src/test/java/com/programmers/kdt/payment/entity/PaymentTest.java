package com.programmers.kdt.payment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentTest {

    @Nested
    @DisplayName("결제 생성")
    class CreatePayment {

        @Test
        @DisplayName("정상적인 orderId, amount로 생성하면 READY 상태로 초기화된다.")
        void createPayment() {
            //given
            Long orderId = 1L;
            Long amount = 10000L;

            //when
            Payment payment = Payment.create(orderId, amount);

            //then
            assertThat(payment.getOrderId()).isEqualTo(orderId);
            assertThat(payment.getAmount()).isEqualTo(amount);
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.READY);
            assertThat(payment.getRefundedAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("orderId가 null이면 예외가 발생한다.")
        void createPaymentNullOrderId() {
            assertThatThrownBy(() -> Payment.create(null, 10000L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("amount가 0이하이면 예외가 발생한다.")
        void createPaymentZeroAmount() {
            assertThatThrownBy(() -> Payment.create(1L, 0L))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Payment.create(1L, -10000L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("결제 승인")
    class ApprovePayment {

        @Test
        @DisplayName("READY 상태에서 승인하면 PAID 상태로 전이된다.")
        void approvePayment() {
            //given
            Payment payment = Payment.create(1L, 10000L);

            //when
            payment.approve();

            //then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        @DisplayName("이미 PAID된 상태면 예외 없이 무시된다.")
        void dismissAlreadyPaid() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();

            //when & then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
            payment.approve();

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        @DisplayName("READY가 아닌 상태에서 승인하면 예외가 발생한다.")
        void approvePaymentInvalidState() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.fail();

            //when & then
            assertThatThrownBy(payment::approve)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패")
    class FailPayment {

        @Test
        @DisplayName("READY 상태에서 실패 처리하면 FAILED로 전이된다.")
        void failPayment() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.fail();
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("이미 FAILED 상태면 예외 없이 무시된다.")
        void dismissAlreadyFailed() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.fail();

            //when & then
            payment.fail();
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("PAID 상태에서 실패 처리하면 예외가 발생한다.")
        void failPaymentInvalidState() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();

            //when & then
            assertThatThrownBy(payment::fail)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("전액 취소")
    class CancelPayment {

        @Test
        @DisplayName("PAID 상태에서 취소하면 CANCELLED로 전이되고 refundedAmount가 amount와 같아진다.")
        void cancelPayment() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();

            //when
            payment.cancel();

            //then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getRefundedAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("PARTIAL_CANCELLED 상태에서 취소하면 잔여 금액까지 전부 취소되어 CANCELLED가 된다.")
        void cancelFromPartialCancelled() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();
            payment.partialCancel(3000L);

            //when
            payment.cancel();

            //then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getRefundedAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("이미 CANCELLED된 상태면 예외 없이 무시한다.")
        void dismissAlreadyCancelled() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();
            payment.cancel();

            //when & then
            payment.cancel();

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getRefundedAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("READY 상태에서 취소하면 예외가 발생한다.")
        void cancelFromReady() {
            Payment payment = Payment.create(1L, 10000L);
            assertThatThrownBy(payment::cancel)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("부분 취소")
    class PartialCancelPayment {

        @Test
        @DisplayName("PAID 상태에서 일부 금액을 취소하면 PARTIAL_CANCELLED로 전이된다.")
        void partialCancelPayment() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();

            //when
            payment.partialCancel(4000L);

            //then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);
            assertThat(payment.getRefundedAmount()).isEqualTo(4000L);
        }

        @Test
        @DisplayName("부분취소를 여러 번 나눠서 하면 refundedAmount가 누적된다.")
        void partialCancelAccumulates() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();

            //when
            payment.partialCancel(3000L);
            payment.partialCancel(4000L);

            //then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);
            assertThat(payment.getRefundedAmount()).isEqualTo(7000L);
        }

        @Test
        @DisplayName("부분취소 누적 합이 amount와 같아지면 CANCELLED로 전이된다.")
        void cancelFromPartialCancelledWithFullyRefundedAmount() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();
            payment.partialCancel(3000L);

            //when
            payment.partialCancel(3000L);
            payment.partialCancel(4000L);

            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getRefundedAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("취소 금액이 null이거나 0 이하이면 예외가 발생한다.")
        void partialCancelFailInvalidAmount() {
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();

            assertThatThrownBy(() -> payment.partialCancel(null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> payment.partialCancel(0L))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> payment.partialCancel(-10000L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("잔여 금액을 초과해서 취소하면 예외가 발생한다.")
        void partialCancelFailInvalidRefundAmount() {
            //given
            Payment payment = Payment.create(1L, 10000L);
            payment.approve();
            payment.partialCancel(7000L);

            //when & then
            assertThatThrownBy(() -> payment.partialCancel(5000L))
                    .isInstanceOf(IllegalArgumentException.class);

        }

        @Test
        @DisplayName("READY 상태에서 부분취소하면 예외가 발생한다.")
        void readyFromPartialCancelled() {
            Payment payment = Payment.create(1L, 10000L);
            assertThatThrownBy(() -> payment.partialCancel(5000L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}