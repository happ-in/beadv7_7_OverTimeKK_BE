package com.programmers.kdt.performance.repository;

import com.programmers.kdt.performance.dto.EndedTicketResponse;
import com.programmers.kdt.performance.dto.PerformanceSessionSeatDto;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Page<Performance> findByPerformanceStatus(PerformanceStatus status, Pageable pageable);

    Page<Performance> findAll(Pageable pageable);

    @Modifying
    @Query("""
         update Performance p
            set p.performanceStatus = :status
          where p.performanceStatus <> :status
            and p.endDate < :now
         """)
    int updateExpiredPerformances(@Param("status") PerformanceStatus status, @Param("now") LocalDate now);

    @Query( value = """
                    select new com.programmers.kdt.performance.dto.EndedTicketResponse(p.performanceId, t.ticketId)
                      from Performance p, Ticket t
                     where p.performanceId = t.performanceId
                       and p.endDate between :start and :end
                       and t.ticketStatus = TicketStatus.RESERVED
                    """)
    List<EndedTicketResponse> findEndedTickets(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
                    select new com.programmers.kdt.performance.dto.PerformanceSessionSeatDto(
                           ps.performanceSessionId.sessionNum, ps.performanceStartAt,
                           ps.actor, psp.zone, psp.price, count(t.ticketId))
                      from Performance p
                      join PerformanceSession ps on ps.performanceSessionId.performanceId = p.performanceId
                      join PerformanceSeatPrice psp on psp.performance.performanceId = p.performanceId
                 left join Ticket t on t.performanceId = p.performanceId
                                    and t.sessionNum = ps.performanceSessionId.sessionNum
                                    and t.zone = psp.zone
                                    and t.ticketStatus = com.programmers.kdt.ticket.entity.TicketStatus.AVAILABLE
                     where p.performanceId = :performanceId
                     group by ps.performanceSessionId.sessionNum, ps.performanceStartAt, ps.actor, psp.zone, psp.price
                     order by ps.performanceSessionId.sessionNum
                    """)
    List<PerformanceSessionSeatDto> findSessionSeatAvailability(@Param("performanceId") Long performanceId);

}
