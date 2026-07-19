package com.programmers.kdt.performance.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Performance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String runtime;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // 티켓 오픈 시각 (ERD: 티켓오픈시간 LocalDateTime NULL)
    private LocalDateTime ticketOpenAt;

    // user-service의 판매자(User)를 ID로만 참조
    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long hallId;
}
