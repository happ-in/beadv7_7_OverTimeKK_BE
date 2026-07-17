package com.programmers.kdt.order.payment.repository;

import com.programmers.kdt.order.payment.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
}
