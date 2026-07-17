package com.programmers.kdt.order.settlement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** TODO: 컬럼 미확정 — 수수료율/환불정책 필드 팀 논의 후 채우기 */
@Entity
@Getter
@NoArgsConstructor
public class SettlementPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;
}
