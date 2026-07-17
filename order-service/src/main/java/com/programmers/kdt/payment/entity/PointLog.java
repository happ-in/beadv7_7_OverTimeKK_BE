package com.programmers.kdt.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointLogId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private PointType type;

    private Long amount;
}
