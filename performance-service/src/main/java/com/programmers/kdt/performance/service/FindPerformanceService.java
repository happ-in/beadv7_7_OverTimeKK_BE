package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.EndedTicketsResponse;
import com.programmers.kdt.performance.dto.FindPerformancesResponse;
import com.programmers.kdt.performance.dto.PerformanceSessionSeatResponse;
import com.programmers.kdt.performance.dto.SellerPerformanceResponse;
import com.programmers.kdt.performance.entity.PerformanceStatus;

import java.time.LocalDate;
import java.util.List;

public interface FindPerformanceService {
    FindPerformancesResponse findPerformances(PerformanceStatus status, int page);

    EndedTicketsResponse findEndedPerformanceTickets(LocalDate from, LocalDate to);

    String getPerformanceTitle(Long performanceId);

    PerformanceSessionSeatResponse findPerformanceSessionSeats(Long performanceId);

    List<SellerPerformanceResponse> findSellerPerformances(Long sellerId);

    FindPerformancesResponse searchPerformancesByTitle(String title);
}
