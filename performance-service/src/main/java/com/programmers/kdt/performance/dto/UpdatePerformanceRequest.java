package com.programmers.kdt.performance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdatePerformanceRequest(
        @NotBlank String title, String description, @NotBlank String runtime,
        @NotNull LocalDate startDate, @NotNull LocalDate endDate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime ticketOpenAt,
        @NotNull Long hallId
) {
}
