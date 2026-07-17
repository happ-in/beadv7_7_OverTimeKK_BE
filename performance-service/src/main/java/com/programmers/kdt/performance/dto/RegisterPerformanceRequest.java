package com.programmers.kdt.performance.dto;

import java.time.LocalDate;

public record RegisterPerformanceRequest(
        String title,
        String description,
        Long runtime,
        LocalDate startDate,
        LocalDate endDate,
        Long hallId,
        Long venueId
) {
}
