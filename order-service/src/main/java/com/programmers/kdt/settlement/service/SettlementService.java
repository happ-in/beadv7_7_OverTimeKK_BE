package com.programmers.kdt.settlement.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.order.service.OrderService;
import com.programmers.kdt.settlement.client.PaymentClient;
import com.programmers.kdt.settlement.client.PerformanceClient;
import com.programmers.kdt.settlement.client.TicketClient;
import com.programmers.kdt.settlement.dto.*;
import com.programmers.kdt.settlement.entity.Settlement;
import com.programmers.kdt.settlement.entity.SettlementDetail;
import com.programmers.kdt.settlement.exception.SettlementErrorCode;
import com.programmers.kdt.settlement.repository.SettlementDetailRepository;
import com.programmers.kdt.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private static final BigDecimal pgFeeRate = BigDecimal.valueOf(0.0374);
    private static final BigDecimal serviceFeeRate = BigDecimal.valueOf(0.05);

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;

    private final PerformanceClient performanceClient;
    private final TicketClient ticketClient;
    private final OrderService orderService;
    private final PaymentClient paymentClient;

    @Transactional
    public void createMonthlySettlements(LocalDate settlementMonth) {

        LocalDate targetMonth = settlementMonth.withDayOfMonth(1);

        LocalDate scheduledSettlementDate = targetMonth
                .plusMonths(1)
                .withDayOfMonth(10);

        // 1. 지난달에 종료된 공연 회차 조회
        List<SettlementSessionResponse> sessions =
                performanceClient.getEndedSessions(targetMonth);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        // 2. 공연 회차별 티켓 조회
        List<GetTicketsRequest> ticketsRequest = sessions.stream()
                .map(session -> new GetTicketsRequest(
                        session.performanceId(),
                        session.sessionNum()
                ))
                .toList();

        List<GetTicketsResponse> tickets =
                ticketClient.getTickets(ticketsRequest);

        if (tickets == null || tickets.isEmpty()) {
            return;
        }

        // 3. 티켓별 주문 조회
        List<Long> ticketIds = tickets.stream()
                .map(GetTicketsResponse::ticketId)
                .toList();

        List<OrderResponse> orders =
                orderService.getOrders(ticketIds);

        if (orders == null || orders.isEmpty()) {
            return;
        }

        // 4. 주문별 결제 조회
        List<Long> orderIds = orders.stream()
                .map(OrderResponse::orderId)
                .toList();

        List<PaymentResponse> payments =
                paymentClient.getPayments(orderIds);

        if (payments == null || payments.isEmpty()) {
            return;
        }

        // 5. 조회 결과를 Map으로 변환

        // ticketId → 티켓 정보
        Map<Long, GetTicketsResponse> ticketMap = tickets.stream()
                .collect(Collectors.toMap(
                        GetTicketsResponse::ticketId,
                        Function.identity()
                ));

        // orderId → ticketId
        Map<Long, Long> orderMap = orders.stream()
                .collect(Collectors.toMap(
                        OrderResponse::orderId,
                        OrderResponse::ticketId
                ));

        // 공연 회차 → sellerId
        Map<SessionKey, Long> sellerMap = sessions.stream()
                .collect(Collectors.toMap(
                        session -> new SessionKey(
                                session.performanceId(),
                                session.sessionNum()
                        ),
                        SettlementSessionResponse::sellerId
                ));

        // 6. 공연 회차별 실매출 합산
        Map<SessionKey, Long> grossAmountMap = new HashMap<>();

        for (PaymentResponse payment : payments) {

            Long ticketId = orderMap.get(payment.orderId());

            if (ticketId == null) {
                continue;
            }

            GetTicketsResponse ticket = ticketMap.get(ticketId);

            if (ticket == null) {
                continue;
            }

            SessionKey sessionKey = new SessionKey(ticket.performanceId(), ticket.sessionNum());

            grossAmountMap.merge(sessionKey, payment.netAmount(), Long::sum);
        }

        // 7. 판매자별 회차 실매출 분류
        Map<Long, List<SessionSalesAmount>> sellerSalesMap = new HashMap<>();

        for (Map.Entry<SessionKey, Long> entry : grossAmountMap.entrySet()) {

            SessionKey sessionKey = entry.getKey();
            Long grossAmount = entry.getValue();

            Long sellerId = sellerMap.get(sessionKey);

            if (sellerId == null) {
                continue;
            }

            SessionSalesAmount salesAmount = new SessionSalesAmount(
                    sessionKey.performanceId(),
                    sessionKey.sessionNum(),
                    grossAmount
            );

            sellerSalesMap
                    .computeIfAbsent(
                            sellerId,
                            ignored -> new ArrayList<>()
                    )
                    .add(salesAmount);
        }

        // 8. 판매자별 정산 생성
        for (Map.Entry<Long, List<SessionSalesAmount>> entry : sellerSalesMap.entrySet()) {

            Long sellerId = entry.getKey();
            List<SessionSalesAmount> salesAmounts = entry.getValue();

            boolean alreadyExists = settlementRepository.existsBySellerIdAndSettlementMonth(sellerId, targetMonth);

            if (alreadyExists) {
                continue;
            }

            createSettlement(sellerId, targetMonth, scheduledSettlementDate, salesAmounts);
        }
    }
    private void createSettlement(
            Long sellerId,
            LocalDate settlementMonth,
            LocalDate scheduledSettlementDate,
            List<SessionSalesAmount> salesAmounts
    ) {
        if (salesAmounts == null || salesAmounts.isEmpty()) {
            return;
        }
        List<SessionSettlementAmount> sessionAmounts = salesAmounts.stream()
                .map(this::calculateSessionAmount)
                .toList();
        Long totalGrossAmount = sessionAmounts.stream()
                .mapToLong(SessionSettlementAmount::grossAmount)
                .sum();
        Long totalPgFeeAmount = sessionAmounts.stream()
                .mapToLong(SessionSettlementAmount::pgFeeAmount)
                .sum();
        Long totalServiceFeeAmount = sessionAmounts.stream()
                .mapToLong(SessionSettlementAmount::serviceFeeAmount)
                .sum();

        Settlement settlement = Settlement.create(sellerId, settlementMonth, scheduledSettlementDate, totalGrossAmount, totalPgFeeAmount, totalServiceFeeAmount);
        Settlement savedSettlement = settlementRepository.save(settlement);

        List<SettlementDetail> details = sessionAmounts.stream()
                .map(session -> SettlementDetail.create(
                        savedSettlement,
                        session.performanceId(),
                        session.sessionNum(),
                        session.grossAmount(),
                        session.pgFeeAmount(),
                        session.serviceFeeAmount()
                ))
                .toList();
        settlementDetailRepository.saveAll(details);
    }

    private SessionSettlementAmount calculateSessionAmount(SessionSalesAmount salesAmount) {
        validateGrossAmount(salesAmount.grossAmount());
        Long pgFeeAmount = calculateFee(salesAmount.grossAmount(), pgFeeRate);
        Long serviceFeeAmount = calculateFee(salesAmount.grossAmount(), serviceFeeRate);
        return new SessionSettlementAmount(
                salesAmount.performanceId(),
                salesAmount.sessionNum(),
                salesAmount.grossAmount(),
                pgFeeAmount,
                serviceFeeAmount
        );
    }

    private void validateGrossAmount(Long grossAmount) {
        if (grossAmount == null || grossAmount < 0) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT);
        }
    }

    private long calculateFee(Long amount, BigDecimal feeRate) {
        return BigDecimal.valueOf(amount)
                .multiply(feeRate)
                .setScale(0, RoundingMode.DOWN)
                .longValueExact();
    }
    private record SessionKey(
            Long performanceId,
            Long sessionNum
    ) {
    }
}