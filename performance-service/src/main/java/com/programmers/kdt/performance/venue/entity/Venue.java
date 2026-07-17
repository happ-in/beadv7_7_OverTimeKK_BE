package com.programmers.kdt.performance.venue.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venueId;

    private String venueName;
    private String roadAddress;
    private String detailAddress;
    private String notice;
}
