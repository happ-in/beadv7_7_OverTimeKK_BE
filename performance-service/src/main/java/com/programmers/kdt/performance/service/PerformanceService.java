package com.programmers.kdt.performance.service;

import com.programmers.kdt.performance.dto.PerformanceDetailResponse;
import com.programmers.kdt.performance.dto.PerformanceRequest;
import com.programmers.kdt.performance.dto.PerformanceResponse;

import java.util.List;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.performance.repository.PerformanceSeatPriceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceSessionRepository performanceSessionRepository;
    private final PerformanceSeatPriceRepository performanceSeatPriceRepository;

    @Transactional
    public PerformanceResponse registerPerformance(PerformanceRequest request, Long sellerId) {
        // TODO: hallId 존재 검증(venue), sellerId 판매자 검증(user)
        Performance performance = Performance.createPerformance(
                request.title(),
                request.description(),
                request.runtime(),
                request.startDate(),
                request.endDate(),
                request.ticketOpenAt(),
                sellerId,
                request.hallId(),
                request.postUrl()
        );

        Performance saved = performanceRepository.save(performance);
        return new PerformanceResponse(saved.getPerformanceId(), saved.getTitle());
    }

    @Transactional
    public PerformanceResponse updatePerformance(Long performanceId, PerformanceRequest request, Long sellerId) {
        Performance performance = getPerformance(performanceId);
        validateOwner(performance, sellerId);
        performance.updatePerformance(
                request.title(),
                request.description(),
                request.runtime(),
                request.startDate(),
                request.endDate(),
                request.ticketOpenAt(),
                request.hallId());
        return new PerformanceResponse(performance.getPerformanceId(), performance.getTitle());
    }

    @Transactional
    public void deletePerformance(Long performanceId, Long sellerId) {
        Performance performance = getPerformance(performanceId);
        validateOwner(performance, sellerId);
        performanceSeatPriceRepository.deleteByPerformance_PerformanceId(performanceId);
        performanceSessionRepository.deleteByPerformanceSessionId_PerformanceId(performanceId);
        performanceRepository.delete(performance);
        // TODO: order-service에 판매/주문 존재 확인 또는 취소 이벤트 발행에 대해서는 논의사항(어느정도의갚아를가져갈지)
    }

    @Transactional(readOnly = true)
    public PerformanceDetailResponse getPerformanceDetail(Long performanceId) {
        return PerformanceDetailResponse.from(getPerformance(performanceId));
    }

    @Transactional(readOnly = true)
    public List<PerformanceDetailResponse> getPerformances() {
        return performanceRepository.findAll().stream()
                .map(PerformanceDetailResponse::from)
                .toList();
    }

    private Performance getPerformance(Long id) {
        return performanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(PerformanceErrorCode.PERFORMANCE_NOT_FOUND));
    }

    private void validateOwner(Performance performance, Long sellerId) {
        if (!performance.getSellerId().equals(sellerId)) {
            throw new BusinessException(PerformanceErrorCode.NOT_PERFORMANCE_OWNER);
        }
    }

    public void cancelPerformance(Long performanceId) {
        // TODO: order-service에 REST로 취소/환불 요청 (OrderClient 필요)
    }
}
