package com.programmers.kdt.performance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.programmers.kdt.performance.entity.Performance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PerformanceDetailResponse(
        Long performanceId,
        String title,
        String description,
        String runtime,
        LocalDate startDate,
        LocalDate endDate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime ticketOpenAt,
        Long hallId
) {
    public static PerformanceDetailResponse from(Performance performance) {
        return new PerformanceDetailResponse(
                performance.getPerformanceId(),
                performance.getTitle(),
                performance.getDescription(),
                performance.getRuntime(),
                performance.getStartDate(),
                performance.getEndDate(),
                performance.getTicketOpenAt(),
                performance.getHallId());
    }
}
