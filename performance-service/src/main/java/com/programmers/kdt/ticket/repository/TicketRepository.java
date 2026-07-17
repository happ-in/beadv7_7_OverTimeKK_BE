package com.programmers.kdt.ticket.repository;

import com.programmers.kdt.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findBySessionNumAndZone(Long sessionNum, String zone);
}
