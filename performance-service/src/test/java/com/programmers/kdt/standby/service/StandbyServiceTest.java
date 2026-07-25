package com.programmers.kdt.standby.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.entity.PerformanceSession;
import com.programmers.kdt.performance.entity.PerformanceSessionId;
import com.programmers.kdt.performance.repository.PerformanceSeatPriceRepository;
import com.programmers.kdt.performance.repository.PerformanceSessionRepository;
import com.programmers.kdt.standby.entity.Standby;
import com.programmers.kdt.standby.entity.StandbyStatus;
import com.programmers.kdt.standby.dto.StandbyRankResponse;
import com.programmers.kdt.standby.exception.StandbyErrorCode;
import com.programmers.kdt.standby.repository.StandbyRepository;
import com.programmers.kdt.ticket.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StandbyServiceTest {

    @Mock
    private StandbyRepository standbyRepository;

    @Mock
    private PerformanceSessionRepository performanceSessionRepository;

    @Mock
    private PerformanceSeatPriceRepository performanceSeatPriceRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private StandbyService standbyService;

    private static final Long USER_ID = 1L;
    private static final Long PERFORMANCE_ID = 10L;
    private static final Long SESSION_NUM = 2L;

    private final PerformanceSession session = mock(PerformanceSession.class);

    private PerformanceSessionId sessionId() {
        return new PerformanceSessionId(SESSION_NUM, PERFORMANCE_ID);
    }

    @Nested
    @DisplayName("대기 신청(applyStandby) - 회차 단위 1회 제한")
    class Apply {

        @Test
        @DisplayName("신청 내역이 없고 zone도 유효하면 대기 신청이 저장된다.")
        void applyWhenNotYetApplied() {
            // given
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.existsByUserIdAndPerformanceSession(USER_ID, session))
                    .willReturn(false);
            given(performanceSeatPriceRepository.findZonesByPerformance_PerformanceId(PERFORMANCE_ID))
                    .willReturn(List.of("A", "B", "C"));
            given(standbyRepository.save(any(Standby.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            standbyService.applyStandby(USER_ID, PERFORMANCE_ID, SESSION_NUM, List.of("A", "B", "C"));

            // then
            verify(standbyRepository).save(any(Standby.class));
        }

        @Test
        @DisplayName("이미 같은 회차에 신청했다면 ALREADY_APPLIED 예외가 발생하고 저장되지 않는다.")
        void rejectDuplicateApplyToSameSession() {
            // given
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.existsByUserIdAndPerformanceSession(USER_ID, session))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() ->
                    standbyService.applyStandby(USER_ID, PERFORMANCE_ID, SESSION_NUM, List.of("A", "B", "C")))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.ALREADY_APPLIED)
                    );
            verify(standbyRepository, never()).save(any(Standby.class));
        }

        @Test
        @DisplayName("같은 회차라면 지망 zone이 달라도 이미 신청했으므로 차단된다.")
        void rejectDuplicateEvenWhenZonesDiffer() {
            // given - 최초 신청은 A/B/C , 전혀 다른 zone(D/E/F)으로 재신청
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.existsByUserIdAndPerformanceSession(USER_ID, session))
                    .willReturn(true);

            // when & then - zone이 달라도 (userId, session) 기준으로 이미 신청 → 차단
            assertThatThrownBy(() ->
                    standbyService.applyStandby(USER_ID, PERFORMANCE_ID, SESSION_NUM, List.of("D", "E", "F")))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.ALREADY_APPLIED)
                    );
            verify(standbyRepository, never()).save(any(Standby.class));
        }
    }

    @Nested
    @DisplayName("대기 신청(applyStandby) - zone 유효성 검증")
    class ZoneValidation {

        @Test
        @DisplayName("요청 zone이 모두 해당 공연이 사용하는 zone이면 저장된다.")
        void applyWithZonesUsedByPerformance() {
            // given - 공연이 사용하는 zone은 A/B/C, 요청은 그 부분집합
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.existsByUserIdAndPerformanceSession(USER_ID, session))
                    .willReturn(false);
            given(performanceSeatPriceRepository.findZonesByPerformance_PerformanceId(PERFORMANCE_ID))
                    .willReturn(List.of("A", "B", "C"));
            given(standbyRepository.save(any(Standby.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            standbyService.applyStandby(USER_ID, PERFORMANCE_ID, SESSION_NUM, List.of("A", "B"));

            // then
            verify(standbyRepository).save(any(Standby.class));
        }

        @Test
        @DisplayName("요청 zone 중 하나라도 해당 공연이 쓰지 않는 값이면 INVALID_ZONE 예외가 발생하고 저장되지 않는다.")
        void rejectZoneNotUsedByPerformance() {
            // given - 공연이 사용하는 zone은 A/B/C 인데 요청에 없는 zone "Z"가 섞임
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.existsByUserIdAndPerformanceSession(USER_ID, session))
                    .willReturn(false);
            given(performanceSeatPriceRepository.findZonesByPerformance_PerformanceId(PERFORMANCE_ID))
                    .willReturn(List.of("A", "B", "C"));

            // when & then
            assertThatThrownBy(() ->
                    standbyService.applyStandby(USER_ID, PERFORMANCE_ID, SESSION_NUM, List.of("A", "Z")))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.INVALID_ZONE)
                    );
            verify(standbyRepository, never()).save(any(Standby.class));
        }
    }

    @Nested
    @DisplayName("대기 신청(applyStandby) - 매진 전제 검증")
    class SoldOutPrecondition {

        @Test
        @DisplayName("요청 zone이 모두 매진이면(잔여 좌석 없음) 대기 신청이 저장된다.")
        void applyWhenAllZonesSoldOut() {
            // given - 유효 zone A/B/C, 잔여 좌석 있는 zone은 없음(빈 목록) → 전부 매진
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.existsByUserIdAndPerformanceSession(USER_ID, session))
                    .willReturn(false);
            given(performanceSeatPriceRepository.findZonesByPerformance_PerformanceId(PERFORMANCE_ID))
                    .willReturn(List.of("A", "B", "C"));
            given(ticketRepository.findAvailableZones(PERFORMANCE_ID, SESSION_NUM, List.of("A", "B")))
                    .willReturn(List.of());
            given(standbyRepository.save(any(Standby.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            standbyService.applyStandby(USER_ID, PERFORMANCE_ID, SESSION_NUM, List.of("A", "B"));

            // then
            verify(standbyRepository).save(any(Standby.class));
        }

        @Test
        @DisplayName("요청 zone 중 하나라도 잔여 좌석이 있으면 ZONE_NOT_SOLD_OUT 예외가 발생하고 저장되지 않는다.")
        void rejectWhenAnyZoneHasAvailableSeats() {
            // given - 유효 zone A/B/C, 그런데 A에는 아직 잔여 좌석이 있음
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.existsByUserIdAndPerformanceSession(USER_ID, session))
                    .willReturn(false);
            given(performanceSeatPriceRepository.findZonesByPerformance_PerformanceId(PERFORMANCE_ID))
                    .willReturn(List.of("A", "B", "C"));
            given(ticketRepository.findAvailableZones(PERFORMANCE_ID, SESSION_NUM, List.of("A", "B")))
                    .willReturn(List.of("A"));

            // when & then
            assertThatThrownBy(() ->
                    standbyService.applyStandby(USER_ID, PERFORMANCE_ID, SESSION_NUM, List.of("A", "B")))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.ZONE_NOT_SOLD_OUT)
                    );
            verify(standbyRepository, never()).save(any(Standby.class));
        }
    }

    @Nested
    @DisplayName("매칭(tryMatch) - zone에 자리가 났을 때(대기1순위일떄) 대기자 선정")
    class TryMatch {

        @Test
        @DisplayName("해당 zone 대기자가 있으면 repository가 FIFO 선두 1명을 HELD로 바뀐다.")
        void matchesEarliestCandidate() {
            // given - (예약취소 혹은 매칭 취소로 인해) match 콜이 왔다고 가정, 그 중 첫 번째가 매칭 대상
            Standby earliest = mock(Standby.class);
            Standby later = mock(Standby.class);

            given(earliest.getStandbyId()).willReturn(100L);
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.findMatchCandidate(session, "A", StandbyStatus.WAITING))
                    .willReturn(Optional.of(earliest));

            // when
            Optional<Long> matchedId = standbyService.tryMatch(PERFORMANCE_ID, SESSION_NUM, "A");

            // then
            assertThat(matchedId).contains(100L);
            verify(earliest).hold("A");
            verify(later, never()).hold(any());
        }

        @Test
        @DisplayName("해당 zone 대기자가 없으면 매칭되지 않고 빈 값을 반환한다.")
        void returnsEmptyWhenNoCandidate() {
            // given
            given(performanceSessionRepository.findById(sessionId()))
                    .willReturn(Optional.of(session));
            given(standbyRepository.findMatchCandidate(session, "A", StandbyStatus.WAITING))
                    .willReturn(Optional.empty());

            // when
            Optional<Long> matchedId = standbyService.tryMatch(PERFORMANCE_ID, SESSION_NUM, "A");

            // then
            assertThat(matchedId).isEmpty();
        }
    }

    @Nested
    @DisplayName("대기 취소(cancelStandby)")
    class CancelStandby {

        private static final Long STANDBY_ID = 1L;

        @Test
        @DisplayName("WAITING 상태의 본인 신청을 취소하면 cancel()만 호출되고, 재매칭은 시도하지 않는다.")
        void cancelWaitingStandby() {
            // given
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(USER_ID);
            given(standby.getStandbyStatus()).willReturn(StandbyStatus.WAITING);

            // when
            standbyService.cancelStandby(STANDBY_ID, USER_ID);

            // then
            verify(standby).cancel();
            verify(standbyRepository, never())
                    .findMatchCandidate(any(), any(), any());
        }

        @Test
        @DisplayName("HELD 상태의 본인 신청을 취소하면 cancel() 후, 매칭됐던 zone으로 다음 대기자에게 재매칭을 시도한다.")
        void cancelHeldStandbyTriggersRematch() {
            // given
            Standby cancelled = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(cancelled));
            given(cancelled.getUserId()).willReturn(USER_ID);
            given(cancelled.getStandbyStatus()).willReturn(StandbyStatus.HELD);
            given(cancelled.getMatchedZone()).willReturn("A");
            given(cancelled.getPerformanceSession()).willReturn(session);
            given(standbyRepository.findMatchCandidate(session, "A", StandbyStatus.WAITING))
                    .willReturn(Optional.empty());

            // when
            standbyService.cancelStandby(STANDBY_ID, USER_ID);

            // then
            verify(cancelled).cancel();
            verify(standbyRepository).findMatchCandidate(session, "A", StandbyStatus.WAITING);
        }

        @Test
        @DisplayName("존재하지 않는 standbyId면 STANDBY_NOT_FOUND 예외가 발생한다.")
        void rejectWhenStandbyNotFound() {
            // given
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> standbyService.cancelStandby(STANDBY_ID, USER_ID))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.STANDBY_NOT_FOUND)
                    );
        }

        @Test
        @DisplayName("본인 신청이 아니면 NOT_STANDBY_OWNER 예외가 발생하고 취소되지 않는다.")
        void rejectWhenNotOwner() {
            // given - 신청자는 USER_ID인데, 다른 사람(999L)이 취소를 시도
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(999L);

            // when & then
            assertThatThrownBy(() -> standbyService.cancelStandby(STANDBY_ID, USER_ID))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.NOT_STANDBY_OWNER)
                    );
            verify(standby, never()).cancel();
        }
    }

    @Nested
    @DisplayName("지망 zone 부분 취소(cancelZone)")
    class CancelZone {

        private static final Long STANDBY_ID = 1L;

        @Test
        @DisplayName("매칭 안 된 zone을 취소하면 엔티티에 위임만 하고, 재매칭은 시도하지 않는다.")
        void delegatesToEntityCancelZoneWithoutRematch() {
            // given - getMatchedZone()이 null이라 취소하려는 "B"와 다름(매칭된 zone이 아님)
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(USER_ID);

            // when
            standbyService.cancelZone(STANDBY_ID, USER_ID, "B");

            // then
            verify(standby).cancelZone("B");
            verify(standbyRepository, never())
                    .findMatchCandidate(any(), any(), any());
        }

        @Test
        @DisplayName("취소한 zone이 매칭돼있던(HELD) zone이면, 같은 zone으로 다음 대기자에게 즉시 재매칭을 시도한다.")
        void cancelMatchedZoneTriggersRematch() {
            // given
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(USER_ID);
            given(standby.getMatchedZone()).willReturn("B");
            given(standby.getPerformanceSession()).willReturn(session);
            given(standbyRepository.findMatchCandidate(session, "B", StandbyStatus.WAITING))
                    .willReturn(Optional.empty());

            // when
            standbyService.cancelZone(STANDBY_ID, USER_ID, "B");

            // then
            verify(standby).cancelZone("B");
            verify(standbyRepository).findMatchCandidate(session, "B", StandbyStatus.WAITING);
        }

        @Test
        @DisplayName("본인 신청이 아니면 NOT_STANDBY_OWNER 예외가 발생하고 취소되지 않는다.")
        void rejectWhenNotOwner() {
            // given
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(999L);

            // when & then
            assertThatThrownBy(() -> standbyService.cancelZone(STANDBY_ID, USER_ID, "B"))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.NOT_STANDBY_OWNER)
                    );
            verify(standby, never()).cancelZone(any());
        }
    }

    @Nested
    @DisplayName("대기 순위 조회(getStandbyRank)")
    class GetStandbyRank {

        private static final Long STANDBY_ID = 1L;
        private final LocalDateTime reservedAt = LocalDateTime.now();

        @Test
        @DisplayName("지망 zone이 1개면, 그 zone에서 나보다 먼저 신청한 인원 수 + 1이 순위로 반환된다.")
        void returnsRankForSingleZone() {
            // given
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(USER_ID);
            given(standby.getStandbyId()).willReturn(STANDBY_ID);
            given(standby.canViewRank()).willReturn(true);
            given(standby.getZone1()).willReturn("A");
            given(standby.getZone2()).willReturn(null);
            given(standby.getZone3()).willReturn(null);
            given(standby.getReservedAt()).willReturn(reservedAt);
            given(standby.getPerformanceSession()).willReturn(session);
            given(standbyRepository.countRankCount(session, "A", StandbyStatus.WAITING, reservedAt))
                    .willReturn(2L);

            // when
            StandbyRankResponse response = standbyService.getStandbyRank(STANDBY_ID, USER_ID);

            // then
            assertThat(response.standbyId()).isEqualTo(STANDBY_ID);
            assertThat(response.zoneRanks())
                    .containsExactly(new StandbyRankResponse.ZoneRank("A", 3L, false));
        }

        @Test
        @DisplayName("지망 zone이 여러 개면, zone별로 각각의 순위가 리스트로 반환된다.")
        void returnsRankForEachZone() {
            // given
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(USER_ID);
            given(standby.getStandbyId()).willReturn(STANDBY_ID);
            given(standby.canViewRank()).willReturn(true);
            given(standby.getZone1()).willReturn("A");
            given(standby.getZone2()).willReturn("B");
            given(standby.getZone3()).willReturn(null);
            given(standby.getReservedAt()).willReturn(reservedAt);
            given(standby.getPerformanceSession()).willReturn(session);
            given(standbyRepository.countRankCount(session, "A", StandbyStatus.WAITING, reservedAt))
                    .willReturn(0L);
            given(standbyRepository.countRankCount(session, "B", StandbyStatus.WAITING, reservedAt))
                    .willReturn(4L);

            // when
            StandbyRankResponse response = standbyService.getStandbyRank(STANDBY_ID, USER_ID);

            // then
            assertThat(response.zoneRanks())
                    .containsExactly(
                            new StandbyRankResponse.ZoneRank("A", 1L, false),
                            new StandbyRankResponse.ZoneRank("B", 5L, false)
                    );
        }

        @Test
        @DisplayName("HELD 상태의 경우에도, 나머지 대기상태 정상 순위로 반환.")
        void returnsGetHeldTest() {
            // given - A매칭(HELD), B는 대기(3번째)
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(USER_ID);
            given(standby.getStandbyId()).willReturn(STANDBY_ID);
            given(standby.canViewRank()).willReturn(true);
            given(standby.getMatchedZone()).willReturn("A");
            given(standby.getZone1()).willReturn("A");
            given(standby.getZone2()).willReturn("B");
            given(standby.getZone3()).willReturn(null);
            given(standby.getReservedAt()).willReturn(reservedAt);
            given(standby.getPerformanceSession()).willReturn(session);
            given(standbyRepository.countRankCount(session, "B", StandbyStatus.WAITING, reservedAt))
                    .willReturn(2L);

            // when
            StandbyRankResponse response = standbyService.getStandbyRank(STANDBY_ID, USER_ID);

            // then
            assertThat(response.zoneRanks())
                    .containsExactly(
                            new StandbyRankResponse.ZoneRank("A", 0L, true),
                            new StandbyRankResponse.ZoneRank("B", 3L, false)
                    );
            verify(standbyRepository, never())
                    .countRankCount(eq(session), eq("A"), any(), any());
        }

        @Test
        @DisplayName("본인 신청이 아니면 NOT_STANDBY_OWNER 예외가 발생한다.")
        void rejectWhenNotOwner() {
            // given
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(999L);

            // when & then
            assertThatThrownBy(() -> standbyService.getStandbyRank(STANDBY_ID, USER_ID))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.NOT_STANDBY_OWNER)
                    );
        }

        @Test
        @DisplayName("WAITING 상태가 아니면(예: 취소됨) CANNOT_VIEW_RANK 예외가 발생한다.")
        void rejectWhenNotWaiting() {
            // given
            Standby standby = mock(Standby.class);
            given(standbyRepository.findById(STANDBY_ID)).willReturn(Optional.of(standby));
            given(standby.getUserId()).willReturn(USER_ID);
            given(standby.canViewRank()).willReturn(false);

            // when & then
            assertThatThrownBy(() -> standbyService.getStandbyRank(STANDBY_ID, USER_ID))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(StandbyErrorCode.CANNOT_VIEW_RANK)
                    );
        }
    }
}
