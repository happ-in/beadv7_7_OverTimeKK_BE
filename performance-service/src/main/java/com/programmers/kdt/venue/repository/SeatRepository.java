package com.programmers.kdt.venue.repository;

import com.programmers.kdt.venue.entity.Hall;
import com.programmers.kdt.venue.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByHall(Hall hall);
}
