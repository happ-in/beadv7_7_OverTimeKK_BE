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

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private LocalDate settlementMonth;

    @Column(nullable = false)
    private LocalDate scheduledSettlementDate;

    @Column(nullable = false)
    private Long grossAmount;

    @Column(nullable = false)
    private Long serviceFeeAmount;

    @Column(nullable = false)
    private Long settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus;

    private LocalDateTime paidAt;

    private Settlement(
            Long sellerId,
            LocalDate settlementMonth,
            LocalDate scheduledSettlementDate,
            Long grossAmount,
            Long serviceFeeAmount
    ) {
        validateRequiredFields(sellerId, settlementMonth, scheduledSettlementDate);
        validateSettlementMonth(settlementMonth);
        validateAmount(grossAmount, serviceFeeAmount);

        this.sellerId = sellerId;
        this.settlementMonth = settlementMonth;
        this.scheduledSettlementDate = scheduledSettlementDate;
        this.grossAmount = grossAmount;
        this.serviceFeeAmount = serviceFeeAmount;
        this.settlementAmount = grossAmount - serviceFeeAmount;
        this.settlementStatus = SettlementStatus.PENDING;
    }

    public static Settlement create(
            Long sellerId,
            LocalDate settlementMonth,
            LocalDate scheduledSettlementDate,
            Long grossAmount,
            Long serviceFeeAmount
    ) {
        return new Settlement(sellerId, settlementMonth, scheduledSettlementDate, grossAmount, serviceFeeAmount);
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

    private void validateAmount(
            Long grossAmount,
            Long serviceFeeAmount
    ) {
        if (grossAmount == null || grossAmount < 0) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT);
        }

        if (serviceFeeAmount == null || serviceFeeAmount < 0) {
            throw new BusinessException(SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT);
        }

        if (serviceFeeAmount > grossAmount) {
            throw new BusinessException(SettlementErrorCode.SERVICE_FEE_EXCEEDS_GROSS_AMOUNT);
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