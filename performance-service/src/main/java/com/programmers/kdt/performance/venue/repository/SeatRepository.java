package com.programmers.kdt.performance.venue.repository;

import com.programmers.kdt.performance.venue.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByHallId(Long hallId);
}
