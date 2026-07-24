package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Page<Performance> findByPerformanceStatus(PerformanceStatus status, Pageable pageable);

    Page<Performance> findAll(Pageable pageable);

    @Modifying
    @Query("""
         update Performance p
            set p.performanceStatus = :status
          where p.performanceStatus <> :status
            and p.endDate < :now 
""")
    int updateExpiredPerformances(@Param("status") PerformanceStatus status, @Param("now") LocalDate now);
}
