package com.programmers.kdt.performance.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class PerformanceSession extends BaseTimeEntity {

    @EmbeddedId
    private PerformanceSessionId performanceSessionId;

    // 복합키의 performanceId 부분을 이 FK가 채움(@MapsId)
    @MapsId("performanceId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private LocalDateTime performanceStartAt;
}

