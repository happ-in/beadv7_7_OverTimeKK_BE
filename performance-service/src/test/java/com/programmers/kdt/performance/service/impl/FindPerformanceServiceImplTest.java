package com.programmers.kdt.performance.service.impl;

import com.programmers.kdt.common.PageConstants;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.performance.dto.EndedTicketResponse;
import com.programmers.kdt.performance.dto.EndedTicketsResponse;
import com.programmers.kdt.performance.dto.FindPerformanceResponse;
import com.programmers.kdt.performance.entity.Performance;
import com.programmers.kdt.performance.entity.PerformanceStatus;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import com.programmers.kdt.performance.repository.PerformanceRepository;
import com.programmers.kdt.venue.entity.Hall;
import com.programmers.kdt.venue.repository.HallRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FindPerformanceServiceImplTest {

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private HallRepository hallRepository;

    @InjectMocks
    private FindPerformanceServiceImpl performanceService;

    @Test
    @DisplayName("공연 전체 조회를 한다.")
    void findAllPerformances() {
        //  given
        // performance
        Performance performance = mock(Performance.class);
        Pageable pageable = PageRequest.of(
                0
                , PageConstants.DEFAULT_PAGE_SIZE
                , Sort.by("startDate", "performanceId").ascending()
        );
        given(performance.getPerformanceId()).willReturn(1L);
        given(performance.getTitle()).willReturn("야근크크");
        given(performance.getHallId()).willReturn(10L);
        given(performanceRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(performance)));


        // hall
        Hall hall = mock(Hall.class);
        given(hall.getHallName()).willReturn("프로그래머스");
        given(hallRepository.findById(performance.getHallId())).willReturn(Optional.of(hall));

        // when
        List<FindPerformanceResponse> responses = performanceService.findPerformances(null, 1);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().title()).isEqualTo("야근크크");
        assertThat(responses.getFirst().hallName()).isEqualTo("프로그래머스");

        verify(performanceRepository).findAll(pageable);
        verify(hallRepository).findById(10L);
    }

    @Test
    @DisplayName("오픈 예정인 공연을 조회한다.")
    void findUpcomingPerformances() {
        // given
        Pageable pageable = PageRequest.of(
                0
                , PageConstants.DEFAULT_PAGE_SIZE
                , Sort.by("startDate", "performanceId").ascending()
        );

        Performance performance = mock(Performance.class);
        given(performance.getPerformanceId()).willReturn(1L);
        given(performance.getHallId()).willReturn(10L);
        given(performanceRepository.findByPerformanceStatus(PerformanceStatus.UPCOMING, pageable)).willReturn(new PageImpl<>(List.of(performance)));

        Hall hall = mock(Hall.class);
        given(hall.getHallName()).willReturn("이웃집 토토로");
        given(hallRepository.findById(performance.getHallId())).willReturn(Optional.of(hall));

        // when
        List<FindPerformanceResponse> responses = performanceService.findPerformances(PerformanceStatus.UPCOMING, 1);

        // then
        assertThat(responses.getFirst().performanceId()).isEqualTo(1L);
        assertThat(responses.getFirst().hallName()).isEqualTo("이웃집 토토로");

        verify(performanceRepository).findByPerformanceStatus(PerformanceStatus.UPCOMING, pageable);
        verify(hallRepository).findById(10L);
    }

    @Test
    @DisplayName("조회된 공연 내역이 없습니다.")
    void notFoundPerformance() {

        // given
        Pageable pageable = PageRequest.of(
                0
                , PageConstants.DEFAULT_PAGE_SIZE
                , Sort.by("startDate", "performanceId").ascending()
        );

        given(performanceRepository.findAll(pageable)).willReturn(Page.empty()); // 값이 없도록 만들기

        // when-then
        assertThatThrownBy(() -> performanceService.findPerformances(null, 1))
                .isInstanceOf(BusinessException.class) // 발생한 예외 타입 확인
                .hasFieldOrPropertyWithValue( // BusinessException.class 내부 값으로 인증
                        "errorCode"
                        , PerformanceErrorCode.FIND_PERFORMANCES_NO_RESULT
                );
        then(hallRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("페이지는 1번부터 조회된다.")
    void pageStartWith1() {
        // given
        assertThatThrownBy(() -> performanceService.findPerformances(null, 0))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode"
                        , CommonErrorCode.PAGE_BAD_REQUEST
                );

        then(performanceRepository).shouldHaveNoInteractions();
        then(hallRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("공연 종료된 공연의 티켓들을 가져온다.")
    void endedPerformanceTickets() {
        // given
        LocalDate endDate = LocalDate.of(2026, 7, 26);
        EndedTicketResponse endedTicket = mock(EndedTicketResponse.class);

        given(performanceRepository.findEndedTickets(endDate)).willReturn(List.of(endedTicket));

        // when
        EndedTicketsResponse endedTickets = performanceService.findEndedPerformanceTickets(endDate);

        // then
        assertThat(endedTickets.endedTickets()).hasSize(1);
        assertThat(endedTickets.date()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("종료된 공연이 없어도 빈 리스트를 반환한다.")
    void endedPerformanceTicketEmpty() {
        // given
        LocalDate endDate = LocalDate.of(2026, 7, 26);

        given(performanceRepository.findEndedTickets(endDate)).willReturn(List.of());

        // when
        EndedTicketsResponse endedTickets = performanceService.findEndedPerformanceTickets(endDate);

        // then
        assertThat(endedTickets.endedTickets()).hasSize(0);
        assertThat(endedTickets.date()).isEqualTo(endDate);
    }
}
