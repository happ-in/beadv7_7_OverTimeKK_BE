package com.programmers.kdt.settlement.repository;

import com.programmers.kdt.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    boolean existsBySellerIdAndSettlementMonth(Long sellerId, LocalDate settlementMonth);
}