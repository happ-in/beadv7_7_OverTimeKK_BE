package com.programmers.kdt.performance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceInfoId;

    private String title;
    private String description;
    private Long runtime;
    private LocalDate startDate;
    private LocalDate endDate;

    // 티켓 오픈 시각 — 정확한 시:분까지 필요해 LocalDateTime으로 지정 (ERD엔 LocalDate로만 표시됨, 팀 확인 필요)
    private LocalDateTime ticketOpenAt;

    // user-service의 판매자(User)를 ID로만 참조
    private Long sellerId;

    private Long hallId;
    private Long venueId;
}
