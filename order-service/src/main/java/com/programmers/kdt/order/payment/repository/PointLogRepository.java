package com.programmers.kdt.order.payment.repository;

import com.programmers.kdt.order.payment.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    List<PointLog> findByUserId(Long userId);
}
