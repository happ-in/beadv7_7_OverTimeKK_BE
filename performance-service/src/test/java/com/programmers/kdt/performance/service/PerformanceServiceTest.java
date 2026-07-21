package com.programmers.kdt.performance.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.dto.PerformanceRequest;
import com.programmers.kdt.performance.dto.PerformanceResponse;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    private PerformanceRepository performanceRepository;

    @InjectMocks
    private PerformanceService performanceService;

    @Test
    void 공연_등록_성공() {
        // given
        PerformanceRequest request = new PerformanceRequest(
                "뮤지컬A", "설명", "120분",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                null, 1L);
        Long sellerId = 1L;

        Performance saved = Performance.createPerformance(
                "뮤지컬A", "설명", "120분",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                null, sellerId, 1L);
        ReflectionTestUtils.setField(saved, "performanceId", 1L);   // @Id는 setter가 없어 테스트에서 주입
        given(performanceRepository.save(any(Performance.class))).willReturn(saved);

        // when
        PerformanceResponse res = performanceService.registerPerformance(request, sellerId);

        // then
        assertThat(res.performanceId()).isEqualTo(1L);
        assertThat(res.title()).isEqualTo("뮤지컬A");
        verify(performanceRepository).save(any(Performance.class));
    }

    @Test
    void 공연_수정_성공() {
        Performance performance = Performance.createPerformance(
                "뮤지컬A", "설명", "120분",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), null, 1L, 1L);
        given(performanceRepository.findById(1L)).willReturn(Optional.of(performance));

        PerformanceRequest request = new PerformanceRequest(
                "뮤지컬B", "수정설명", "130분",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null, 2L);

        performanceService.updatePerformance(1L, request, 1L);

        assertThat(performance.getTitle()).isEqualTo("뮤지컬B");
        assertThat(performance.getHallId()).isEqualTo(2L);
    }

    @Test
    void 없는_공연_수정시_예외() {
        given(performanceRepository.findById(999L)).willReturn(Optional.empty());
        PerformanceRequest request = new PerformanceRequest(
                "뮤지컬B", "설명", "130분",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null, 2L);

        assertThatThrownBy(() -> performanceService.updatePerformance(999L, request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(PerformanceErrorCode.PERFORMANCE_NOT_FOUND.getMessage());
    }

    @Test
    void 판매자_본인이_아니면_수정_예외() {
        Performance performance = Performance.createPerformance(
                "뮤지컬A", "설명", "120분",
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), null, 1L, 1L);
        given(performanceRepository.findById(1L)).willReturn(Optional.of(performance));

        PerformanceRequest request = new PerformanceRequest(
                "뮤지컬B", "설명", "130분",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null, 2L);

        assertThatThrownBy(() -> performanceService.updatePerformance(1L, request, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(PerformanceErrorCode.NOT_PERFORMANCE_OWNER.getMessage());
    }
}
