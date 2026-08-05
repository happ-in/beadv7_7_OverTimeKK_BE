package com.programmers.kdt.settlement.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.settlement.exception.SettlementErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "settlement_details",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_detail_session",
                        columnNames = {
                                "performance_id",
                                "session_num"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementDetailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(name = "session_num", nullable = false)
    private Long sessionNum;

    @Column(name = "gross_amount", nullable = false)
    private Long grossAmount;

    @Column(name = "pg_fee_amount", nullable = false)
    private Long pgFeeAmount;

    @Column(name = "service_fee_amount", nullable = false)
    private Long serviceFeeAmount;

    @Column(name = "settlement_amount", nullable = false)
    private Long settlementAmount;

    private SettlementDetail(
            Settlement settlement,
            Long performanceId,
            Long sessionNum,
            Long grossAmount,
            Long pgFeeAmount,
            Long serviceFeeAmount
    ) {
        validateRequiredFields(settlement, performanceId, sessionNum);
        validateAmount(grossAmount, pgFeeAmount, serviceFeeAmount);

        this.settlement = settlement;
        this.performanceId = performanceId;
        this.sessionNum = sessionNum;
        this.grossAmount = grossAmount;
        this.pgFeeAmount = pgFeeAmount;
        this.serviceFeeAmount = serviceFeeAmount;
        this.settlementAmount =
                grossAmount - pgFeeAmount - serviceFeeAmount;
    }

    public static SettlementDetail create(
            Settlement settlement,
            Long performanceId,
            Long sessionNum,
            Long grossAmount,
            Long pgFeeAmount,
            Long serviceFeeAmount
    ) {
        return new SettlementDetail(settlement, performanceId, sessionNum, grossAmount, pgFeeAmount, serviceFeeAmount);
    }

    private void validateRequiredFields(Settlement settlement, Long performanceId, Long sessionNum) {
        if (settlement == null) {
            throw new BusinessException(
                    SettlementErrorCode.SETTLEMENT_REQUIRED
            );
        }

        if (performanceId == null) {
            throw new BusinessException(
                    SettlementErrorCode.PERFORMANCE_ID_REQUIRED
            );
        }

        if (sessionNum == null) {
            throw new BusinessException(
                    SettlementErrorCode.SESSION_NUM_REQUIRED
            );
        }
    }

    private void validateAmount(Long grossAmount, Long pgFeeAmount, Long serviceFeeAmount) {
        if (grossAmount == null || grossAmount < 0) {
            throw new BusinessException(
                    SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT
            );
        }

        if (pgFeeAmount == null || pgFeeAmount < 0) {
            throw new BusinessException(
                    SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT
            );
        }

        if (serviceFeeAmount == null || serviceFeeAmount < 0) {
            throw new BusinessException(
                    SettlementErrorCode.INVALID_SETTLEMENT_AMOUNT
            );
        }

        if (pgFeeAmount + serviceFeeAmount > grossAmount) {
            throw new BusinessException(
                    SettlementErrorCode.FEE_EXCEEDS_GROSS_AMOUNT
            );
        }
    }
}