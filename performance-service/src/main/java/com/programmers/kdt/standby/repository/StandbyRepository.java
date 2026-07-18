package com.programmers.kdt.standby.repository;

import com.programmers.kdt.standby.entity.Standby;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StandbyRepository extends JpaRepository<Standby, Long> {
    List<Standby> findBySessionNumOrderByReservedAtAsc(Long sessionNum);
}
