package com.programmers.kdt.ticket.repository;

import com.programmers.kdt.ticket.dto.OrderTicketResponse;
import com.programmers.kdt.ticket.dto.TicketZoneResponse;
import com.programmers.kdt.ticket.entity.Ticket;
import com.programmers.kdt.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByTicketStatusAndHoldExpiredAtBefore(TicketStatus ticketStatus, LocalDateTime dateTime);

    // 요청 zone 중 아직 잔여 좌석(AVAILABLE)이 남아있는 zone 목록.
    // 비어 있으면 = 요청 zone 전부 매진 → 대기 신청 가능.
    @Query("select distinct t.zone from Ticket t " +
            "where t.performanceId = :performanceId and t.sessionNum = :sessionNum " +
            "and t.zone in :zones " +
            "and t.ticketStatus = com.programmers.kdt.ticket.entity.TicketStatus.AVAILABLE")
    List<String> findAvailableZones(@Param("performanceId") Long performanceId,
                                   @Param("sessionNum") Long sessionNum,
@Param("zones") List<String> zones);
        
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
   and p.hallId = s.hallId
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
   and p.hallId = s.hallId
   and psp.zone = s.zone
   and p.performanceId = :performanceId
    """)
    int issueAdditionalTickets(@Param("performanceId") Long performanceId, @Param("sessionNum") Long sessionNum, @Param("ticketStatus") TicketStatus status);

    void deleteByPerformanceIdAndSessionNum(Long performanceId, Long sessionNum);

    boolean existsByPerformanceIdAndSessionNumAndTicketStatusAndZone(Long performanceId, Long sessionNum, TicketStatus ticketStatus, String zone);

    @Query("""
           select new com.programmers.kdt.ticket.dto.OrderTicketResponse(t.ticketId, p.title, t.zone)
             from Ticket t, Performance p
            where t.buyUserId = :userId
              and t.performanceId = p.performanceId
           order by p.endDate desc
           """)
    List<OrderTicketResponse> findTicketsByBuyUserId(@Param("userId") Long userId);

    @Query("""
            select new com.programmers.kdt.ticket.dto.TicketZoneResponse(t.ticketId, t.seatRow, t.seatNum, t.ticketStatus)
              from Ticket t
             where t.performanceId = :performanceId
               and t.sessionNum = :sessionNum
               and t.zone = :zone
            """)
    List<TicketZoneResponse> findByZoneAndPerformance(@Param("performanceId") Long performanceId, @Param("sessionNum") Long sessionNum, @Param("zone") String zone);
}
