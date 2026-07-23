package com.programmers.kdt.performance.dto;

import java.time.LocalDate;

public record FindPerformanceResponse(
        Long performanceId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        String hallName
) {

}
