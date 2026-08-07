package com.programmers.kdt.performance.service.impl;

import com.programmers.kdt.common.PageConstants;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.image.service.S3ImageService;
import com.programmers.kdt.performance.dto.EndedTicketResponse;
import com.programmers.kdt.performance.dto.EndedTicketsResponse;
import com.programmers.kdt.performance.dto.FindPerformanceDto;
import com.programmers.kdt.performance.dto.FindPerformancesResponse;
import com.programmers.kdt.performance.dto.PerformanceSessionSeatDto;
import com.programmers.kdt.performance.dto.PerformanceSessionSeatResponse;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.performance.service.FindPerformanceService;
import com.programmers.kdt.venue.entity.Hall;
import com.programmers.kdt.venue.exception.VenueErrorCode;
import com.programmers.kdt.venue.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindPerformanceServiceImpl implements FindPerformanceService {

    private final PerformanceRepository performanceRepository;
    private final HallRepository hallRepository;
    private final S3ImageService imageService;

    @Override
    public FindPerformancesResponse findPerformances(PerformanceStatus status, int page) {
        if (page <= 0) {
            throw new BusinessException(CommonErrorCode.PAGE_BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page-1, PageConstants.DEFAULT_PAGE_SIZE, Sort.by("startDate", "performanceId").ascending());
        Page<Performance> performances = findPerformances(status, pageable);

        if (performances.isEmpty()) {
            throw new BusinessException(PerformanceErrorCode.FIND_PERFORMANCES_NO_RESULT);
        }

        long pageCount = (long) Math.ceil((double) performances.getTotalElements() / PageConstants.DEFAULT_PAGE_SIZE);
        return new FindPerformancesResponse(pageCount, convertToResponse(performances));
    }

    @Override
    @Transactional(readOnly = true)
    public EndedTicketsResponse findEndedPerformanceTickets(LocalDate from, LocalDate to) {
        log.info("요청기간 : {} ~ {}", from, to);
        List<EndedTicketResponse> endedTickets = performanceRepository.findEndedTickets(from, to);
        return new EndedTicketsResponse(from, to, endedTickets);
    }

    @Override
    public String getPerformanceTitle(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId).orElseThrow(
                () -> new BusinessException(PerformanceErrorCode.PERFORMANCE_NOT_FOUND)
        );
        return performance.getTitle();
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceSessionSeatResponse findPerformanceSessionSeats(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new BusinessException(PerformanceErrorCode.PERFORMANCE_NOT_FOUND));
        List<PerformanceSessionSeatDto> sessions = performanceRepository.findSessionSeatAvailability(performanceId);
        return new PerformanceSessionSeatResponse(performance.getPerformanceId(), performance.getTicketOpenAt(), sessions);
    }

    private Page<Performance> findPerformances(PerformanceStatus status, Pageable pageable) {
        if (status == null ) {
            return performanceRepository.findAll(pageable);
        }
        return performanceRepository.findByPerformanceStatus(status, pageable);
    }

    private @NonNull List<FindPerformanceDto> convertToResponse(Page<Performance> performances) {
        List<FindPerformanceDto> responses = new ArrayList<>();
        for (Performance performance : performances) {
            Hall hall = hallRepository.findById(performance.getHallId())
                    .orElseThrow(() -> new BusinessException(VenueErrorCode.HALL_INFORMATION_NOT_FOUND));

            FindPerformanceDto response = new FindPerformanceDto(
                    performance.getPerformanceId(),
                    performance.getTitle(),
                    performance.getStartDate(),
                    performance.getEndDate(),
                    hall.getHallName(),
                    imageService.getPresignedGetUrl(performance.getImgPath())
            );
            responses.add(response);
        }
        return responses;
    }
}
