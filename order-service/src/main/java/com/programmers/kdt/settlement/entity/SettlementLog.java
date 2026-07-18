package com.programmers.kdt.settlement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class SettlementLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    // performance-service의 Performance를 ID로만 참조
    private Long performanceInfoId;
    private Long sellerId;
    private LocalDate periodDate;
    private Long settlementFee;
    private Long charge;

    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;
}
