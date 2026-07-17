package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.PerformanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PerformanceSessionRepository extends JpaRepository<PerformanceSession, Long> {
    List<PerformanceSession> findByPerfomanceInfoId(Long perfomanceInfoId);
}
