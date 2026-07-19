package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSeatPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerformanceSeatPriceRepository extends JpaRepository<PerformanceSeatPrice, Long> {
    Optional<PerformanceSeatPrice> findByPerformanceAndHallIdAndZone(
            Performance performance, Long hallId, String zone);
}
