package com.programmers.kdt.settlement.repository;

import com.programmers.kdt.settlement.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    boolean existsByPerformanceIdAndSessionNum(Long performanceId, Long sessionNum);
}