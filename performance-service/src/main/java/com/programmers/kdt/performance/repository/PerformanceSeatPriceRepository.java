package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.PerformanceSeatPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceSeatPriceRepository extends JpaRepository<PerformanceSeatPrice, Long> {

    void deleteByPerformance_PerformanceId(Long performanceId);
}
