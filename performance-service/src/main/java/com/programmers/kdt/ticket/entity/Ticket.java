package com.programmers.kdt.ticket.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Ticket extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(name = "session_num", nullable = false)
    private Long sessionNum;

    @Column(name = "zone", nullable = false)
    private String zone;

    @Column(name = "seat_row", nullable = false)
    private String seatRow;

    @Column(name = "seat_num", nullable = false)
    private String seatNum;

    @Column(name = "price", nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false)
    @ColumnDefault("'AVAILABLE'")
    private TicketStatus ticketStatus = TicketStatus.AVAILABLE;

    @Column(name = "buy_user_id")
    private Long buyUserId;

    @Column(name = "hold_expired_at")
    private LocalDateTime holdExpiredAt;

    @Column(name = "standby_user_id")
    private Long standbyUserId;

    @Column(name = "standby_expired_at")
    private LocalDateTime standbyExpiredAt;

    public static Ticket create(Long performanceId, Long sessionNum, String zone, String seatRow, String seatNum, Long price) {
        Ticket ticket = new Ticket();
        ticket.performanceId = performanceId;
        ticket.sessionNum = sessionNum;
        ticket.zone = zone;
        ticket.seatRow = seatRow;
        ticket.seatNum = seatNum;
        ticket.price = price;
        return ticket;
    }

    public void holdTicket (Long userId, LocalDateTime holdExpiredAt) {
        this.buyUserId = userId;
        this.holdExpiredAt = holdExpiredAt;
        this.ticketStatus = TicketStatus.HOLD;
    }

    public void standbyTicket(Long standbyUserId, LocalDateTime standbyExpiredAt) {
        this.standbyUserId = standbyUserId;
        this.standbyExpiredAt = standbyExpiredAt;
    }

    public void releaseToAvailable() {
        this.ticketStatus = TicketStatus.AVAILABLE;
        this.buyUserId = null;
    }

    public void releaseToStandby() {
        this.ticketStatus = TicketStatus.CANCELED;
        this.buyUserId = null;
    }
}
