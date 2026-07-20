package com.programmers.kdt.performance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterPerformanceRequest(
        @NotBlank String title,
        String description,             //nullable
        @NotBlank String runtime,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime ticketOpenAt,     // nullable
        @NotNull Long hallId
) {
}
