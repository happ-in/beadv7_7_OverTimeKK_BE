package com.programmers.kdt.performance.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PerformanceSessionRequest(
        @NotNull Long sessionId,
        @NotNull Long performanceId,
        @NotBlank  String actor,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime performanceStartAt
) {

    public PerformanceSession toEntity(Performance performance) {
        return PerformanceSession.create(
                new PerformanceSessionId(sessionId, performanceId),
                performance,
                actor,
                performanceStartAt);
    }
}
