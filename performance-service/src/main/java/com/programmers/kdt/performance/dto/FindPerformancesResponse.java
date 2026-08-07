package com.programmers.kdt.performance.dto;

import java.util.List;

public record FindPerformancesResponse(
        Long pageCount,
        List<FindPerformanceDto> performances
) {
}
