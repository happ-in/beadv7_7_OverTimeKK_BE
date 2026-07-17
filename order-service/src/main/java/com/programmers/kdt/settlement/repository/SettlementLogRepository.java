package com.programmers.kdt.settlement.repository;

import com.programmers.kdt.settlement.entity.SettlementLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementLogRepository extends JpaRepository<SettlementLog, Long> {
}
