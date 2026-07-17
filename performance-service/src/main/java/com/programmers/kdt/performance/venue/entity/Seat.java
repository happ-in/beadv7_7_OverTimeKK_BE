package com.programmers.kdt.performance.venue.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    private Long hallId;
    private Long venueId;
    private String zone;
    private String seatRow;
    private String seatNum;
    private Long price;
}
