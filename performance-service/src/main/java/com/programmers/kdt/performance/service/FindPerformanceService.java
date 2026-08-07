package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.EndedTicketsResponse;
import com.programmers.kdt.performance.dto.FindPerformancesResponse;
import com.programmers.kdt.performance.dto.PerformanceSessionSeatResponse;
import com.programmers.kdt.performance.entity.PerformanceStatus;

import java.time.LocalDate;

public interface FindPerformanceService {
    FindPerformancesResponse findPerformances(PerformanceStatus status, int page);

    EndedTicketsResponse findEndedPerformanceTickets(LocalDate from, LocalDate to);

    String getPerformanceTitle(Long performanceId);

    PerformanceSessionSeatResponse findPerformanceSessionSeats(Long performanceId);
}
