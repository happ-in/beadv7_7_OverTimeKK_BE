package com.programmers.kdt.performance.service;

import com.programmers.kdt.common.PageConstants;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.performance.dto.FindPerformanceResponse;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.venue.entity.Hall;
import com.programmers.kdt.venue.exception.VenueException;
import com.programmers.kdt.venue.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FindPerformanceService {

    private final PerformanceRepository performanceRepository;
    private final HallRepository hallRepository;
    
    public List<FindPerformanceResponse> findPerformancesByStatus(PerformanceStatus status, int page) {
        if (page <= 0) {
            throw new BusinessException(CommonErrorCode.PAGE_BAD_REQUEST);
        }
        
        Pageable pageable = PageRequest.of(page-1, PageConstants.DEFAULT_PAGE_SIZE, Sort.by("startDate", "performanceId").ascending());
        Page<Performance> performances = findPerformances(status, pageable);

        if (performances.isEmpty()) {
            throw new BusinessException(PerformanceErrorCode.FIND_PERFORMANCES_NO_RESULT);
        }

        return convertToResponse(performances);
    }

    private Page<Performance> findPerformances(PerformanceStatus status, Pageable pageable) {
        if (status == null ) {
            return performanceRepository.findAll(pageable);
        }
        return performanceRepository.findByPerformanceStatus(status, pageable);
    }

    private @NonNull List<FindPerformanceResponse> convertToResponse(Page<Performance> performances) {
        List<FindPerformanceResponse> responses = new ArrayList<>();
        for (Performance performance : performances) {
            Hall hall = hallRepository.findById(performance.getHallId())
                    .orElseThrow(() -> new BusinessException(VenueException.HALL_INFORMATION_NOT_FOUND));

            FindPerformanceResponse response = new FindPerformanceResponse(
                    performance.getPerformanceId(),
                    performance.getTitle(),
                    performance.getStartDate(),
                    performance.getEndDate(),
                    hall.getHallName()
            );
            responses.add(response);
        }
        return responses;
    }
}
