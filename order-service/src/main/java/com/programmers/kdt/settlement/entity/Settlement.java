package com.programmers.kdt.settlement.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.settlement.exception.SettlementErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "settlements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_seller_period",
                        columnNames = {
                                "seller_id",
                                "settlement_month"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "settlement_month", nullable = false)
    private LocalDate settlementMonth;

    @Column(name = "scheduled_settlement_date", nullable = false)
    private LocalDate scheduledSettlementDate;

    @Column(name = "gross_amount", nullable = false)
    private Long grossAmount;

    @Column(name = "pg_fee_amount", nullable = false)
    private Long pgFeeAmount;

    @Column(name = "service_fee_amount", nullable = false)
    private Long serviceFeeAmount;

    @Column(name = "settlement_amount", nullable = false)
    private Long settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    private Settlement(
            Long sellerId,
            LocalDate settlementMonth,
            LocalDate scheduledSettlementDate,
            Long grossAmount,
            Long pgFeeAmount,
            Long serviceFeeAmount
    ) {
        validateRequiredFields(sellerId, settlementMonth, scheduledSettlementDate);
        validateSettlementMonth(settlementMonth);
        validateScheduledSettlementDate(settlementMonth, scheduledSettlementDate);
        validateAmount(grossAmount,pgFeeAmount,serviceFeeAmount);

        this.sellerId = sellerId;
        this.settlementMonth = settlementMonth;
        this.scheduledSettlementDate = scheduledSettlementDate;
        this.grossAmount = grossAmount;
        this.pgFeeAmount = pgFeeAmount;
        this.serviceFeeAmount = serviceFeeAmount;
        this.settlementAmount = grossAmount - (pgFeeAmount + serviceFeeAmount);
        this.settlementStatus = SettlementStatus.PENDING;
    }

    public static Settlement create(
            Long sellerId,
            LocalDate settlementMonth,
            LocalDate scheduledSettlementDate,
            Long grossAmount,
            Long pgFeeAmount,
            Long serviceFeeAmount
    ) {
        return new Settlement(sellerId, settlementMonth, scheduledSettlementDate, grossAmount, pgFeeAmount, serviceFeeAmount);
    }

    private void validateRequiredFields(
            Long sellerId,
            LocalDate settlementMonth,
            LocalDate scheduledSettlementDate
    ) {
        if (sellerId == null) {
            throw new BusinessException(SettlementErrorCode.SELLER_ID_REQUIRED);
        }

        if (settlementMonth == null) {
            throw new BusinessException(SettlementErrorCode.SETTLEMENT_MONTH_REQUIRED);
        }

        if (scheduledSettlementDate == null) {
            throw new BusinessException(SettlementErrorCode.SCHEDULED_SETTLEMENT_DATE_REQUIRED);
        }
    }

    private void validateSettlementMonth(LocalDate settlementMonth) {
        if (settlementMonth.getDayOfMonth() != 1) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_MONTH);
        }
    }
    private void validateScheduledSettlementDate(LocalDate settlementMonth, LocalDate scheduledSettlementDate) {
        if (scheduledSettlementDate.isBefore(settlementMonth)) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_DATE);
        }
    }

    private void validateAmount(Long grossAmount, Long pgFeeAmount, Long serviceFeeAmount) {
        if (grossAmount == null || grossAmount < 0) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT);
        }

        if (pgFeeAmount == null || pgFeeAmount < 0) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT);
        }

        if (serviceFeeAmount == null || serviceFeeAmount < 0) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT);
        }

        if (pgFeeAmount + serviceFeeAmount > grossAmount) {
            throw new BusinessException(SettlementErrorCode.FEE_EXCEEDS_GROSS_AMOUNT);
        }
    }

    public void complete(LocalDateTime paidAt) {
        if (paidAt == null) {
            throw new BusinessException(SettlementErrorCode.PAID_AT_REQUIRED);
        }

        if (settlementStatus != SettlementStatus.PENDING && settlementStatus != SettlementStatus.FAILED) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS);
        }

        this.settlementStatus = SettlementStatus.PAID;
        this.paidAt = paidAt;
    }

    public void fail() {
        if (settlementStatus != SettlementStatus.PENDING) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS);
        }

        this.settlementStatus = SettlementStatus.FAILED;
    }
}