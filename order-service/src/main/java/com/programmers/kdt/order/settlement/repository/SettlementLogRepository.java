package com.programmers.kdt.order.settlement.repository;

import com.programmers.kdt.order.settlement.entity.SettlementLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementLogRepository extends JpaRepository<SettlementLog, Long> {
}
