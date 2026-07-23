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

@Entity
@Getter
@NoArgsConstructor
public class Ticket extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Column(nullable = false)
    private Long performanceId;

    @Column(nullable = false)
    private Long sessionNum;

    @Column(nullable = false)
    private String zone;

    @Column(nullable = false)
    private String seatRow;

    @Column(nullable = false)
    private String seatNum;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'AVAILABLE'")
    private TicketStatus ticketStatus = TicketStatus.AVAILABLE;

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
}
