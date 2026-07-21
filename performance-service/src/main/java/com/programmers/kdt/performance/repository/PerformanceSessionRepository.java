package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceSessionRepository extends JpaRepository<PerformanceSession, PerformanceSessionId> {
    List<PerformanceSession> findByPerformanceSessionId_PerformanceId(Long performanceId);

    void deleteByPerformanceSessionId_PerformanceId(Long performanceId);
}
