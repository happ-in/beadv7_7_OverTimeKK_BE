package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceSessionRepository extends JpaRepository<PerformanceSession, PerformanceSessionId> {
    Optional<List<PerformanceSession>> findByPerformanceSessionId_PerformanceId(Long performanceId);

    void deleteByPerformanceSessionId_PerformanceId(Long performanceId);
}
