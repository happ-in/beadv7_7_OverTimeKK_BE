package com.programmers.kdt.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFilter;

import java.time.LocalDate;

public record SessionStartDateResponse(
        @JsonFilter("YYYY-mm-dd")
        LocalDate performanceDate
) {
}
