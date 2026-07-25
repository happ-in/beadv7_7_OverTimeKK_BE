package com.programmers.kdt.ticket.repository;

import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByPerformanceIdAndSessionNumAndZone(
            Long performanceId, Long sessionNum, String zone);

    @Modifying
    @Query("""
insert into Ticket (performanceId, sessionNum, zone, seatRow, seatNum, price, ticketStatus, createdAt, modifiedAt)
select p.performanceId
     , ps.performanceSessionId.sessionNum
     , s.zone
     , s.seatRow
     , s.seatNum
     , psp.price
     , :ticketStatus
     , local_datetime
     , local_datetime
  from Performance p
     , PerformanceSession ps
     , PerformanceSeatPrice psp
     , Seat s
 where p.performanceId = psp.performance.performanceId
   and p.performanceId = ps.performance.performanceId
   and p.hallId = s.hall.hallId
   and psp.zone = s.zone
   and p.performanceId = :performanceId
""")
    int issueTicket(@Param("performanceId") Long performanceId, @Param("ticketStatus")TicketStatus status);

    @Modifying
    @Query("""
insert into Ticket (performanceId, sessionNum, zone, seatRow, seatNum, price, ticketStatus, createdAt, modifiedAt)
select p.performanceId
     , :sessionNum
     , s.zone
     , s.seatRow
     , s.seatNum
     , psp.price
     , :ticketStatus
     , local_datetime
     , local_datetime
  from Performance p
     , PerformanceSeatPrice psp
     , Seat s
 where p.performanceId = psp.performance.performanceId
   and p.hallId = s.hall.hallId
   and psp.zone = s.zone
   and p.performanceId = :performanceId
""")
    int issueAdditionalTickets(@Param("performanceId") Long performanceId, @Param("sessionNum") Long sessionNum, @Param("ticketStatus") TicketStatus status);

    void deleteByPerformanceIdAndSessionNum(Long performanceId, Long sessionNum);

    boolean existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(Long performanceId, Long sessionNum, TicketStatus ticketStatus, String zone);
}
