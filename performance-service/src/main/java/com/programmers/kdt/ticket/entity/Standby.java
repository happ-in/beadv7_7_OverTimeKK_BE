package com.programmers.kdt.ticket.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 대기 (ERD: Standby)
 * TODO: zone1/zone2/zone3 구조 팀 회의에서 재확인 필요
 */
@Entity
@Getter
@NoArgsConstructor
public class Standby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long standbyId;

    // user-service의 User를 ID로만 참조
    private Long userId;

    private Long perfomanceInfoId;
    private Long sessionNum;

    private String zone1;
    private String zone2;
    private String zone3;

    @Enumerated(EnumType.STRING)
    private StandbyStatus standbyStatus;

    private LocalDateTime reservedAt;
}
