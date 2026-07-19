package com.programmers.kdt.standby.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import com.programmers.kdt.performance.entity.PerformanceSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 대기 (ERD: Standby)
 */
@Entity
@Getter
@NoArgsConstructor
public class Standby extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long standbyId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne
    private PerformanceSession performanceSession;

    @Column(nullable = false)
    private String zone1;

    private String zone2;

    private String zone3;

    @Enumerated(EnumType.STRING)
    private StandbyStatus standbyStatus;

    private LocalDateTime reservedAt;
}
