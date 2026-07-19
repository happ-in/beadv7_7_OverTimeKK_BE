package com.programmers.kdt.venue.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Hall extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hallId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Venue venue;

    @Column(nullable = false)
    private String hallName;

    @Column(nullable = false)
    private Long seatTotalCount;

    @OneToMany(mappedBy = "hall", fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();
}
