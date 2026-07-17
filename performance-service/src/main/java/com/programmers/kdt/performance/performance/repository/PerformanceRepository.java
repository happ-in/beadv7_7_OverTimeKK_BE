package com.programmers.kdt.performance.performance.repository;

import com.programmers.kdt.performance.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
}
