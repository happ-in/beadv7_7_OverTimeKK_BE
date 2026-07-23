package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Page<Performance> findByPerformanceStatus(PerformanceStatus status, Pageable pageable);

    Page<Performance> findAll(Pageable pageable);
}
