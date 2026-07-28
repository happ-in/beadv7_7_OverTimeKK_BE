package com.programmers.kdt.settlement.repository;

import com.programmers.kdt.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementLogRepository extends JpaRepository<Settlement, Long> {
}
