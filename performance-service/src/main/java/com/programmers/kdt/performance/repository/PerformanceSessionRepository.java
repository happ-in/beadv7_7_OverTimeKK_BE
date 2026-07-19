package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PerformanceSessionRepository extends JpaRepository<PerformanceSession, PerformanceSessionId> {
    List<PerformanceSession> findByPerformance(Performance performance);
}
