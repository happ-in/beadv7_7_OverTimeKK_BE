package com.programmers.kdt.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    private Long perfomanceInfoId;
    private Long sessionNum;
    private Long stadiumId;
    private String zone;
    private String seatRow;
    private String seatNum;
    private Long price;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    private Long heldByUserId;
    private LocalDateTime holdExpireAt;
}
