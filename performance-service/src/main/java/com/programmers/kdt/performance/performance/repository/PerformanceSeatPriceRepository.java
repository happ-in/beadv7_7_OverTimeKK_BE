package com.programmers.kdt.performance.performance.repository;

import com.programmers.kdt.performance.performance.entity.PerformanceSeatPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerformanceSeatPriceRepository extends JpaRepository<PerformanceSeatPrice, Long> {
    Optional<PerformanceSeatPrice> findByPerfomanceInfoIdAndHallIdAndZone(
            Long perfomanceInfoId, Long hallId, String zone);
}
