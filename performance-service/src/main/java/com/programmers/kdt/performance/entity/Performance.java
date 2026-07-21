package com.programmers.kdt.performance.entity;

import com.programmers.kdt.common.entity.BaseTimeEntity;
import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.performance.exception.PerformanceErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Performance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String runtime;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // 티켓 오픈 시각 (ERD: 티켓오픈시간 LocalDateTime NULL)
    private LocalDateTime ticketOpenAt;

    // user-service의 판매자(User)를 ID로만 참조
    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long hallId;


    public static Performance createPerformance(String title, String description, String runtime,
                                     LocalDate startDate, LocalDate endDate, LocalDateTime ticketOpenAt,
                                     Long sellerId, Long hallId) {
        validatePerformance(startDate, endDate, ticketOpenAt);
        Performance performance = new Performance();
        performance.title = title;
        performance.description = description;
        performance.runtime = runtime;
        performance.startDate = startDate;
        performance.endDate = endDate;
        performance.ticketOpenAt = ticketOpenAt;
        performance.sellerId = sellerId;
        performance.hallId = hallId;
        return performance;
    }

    // TODO: 티켓 오픈(ticketOpenAt) 이후에는 판매자가 좌석가(PerformanceSeatPrice) 변경 불가.
    //  좌석가격 수정 로직 추후 추가 및 검증 필요
    public void updatePerformance(String title, String description, String runtime,
                       LocalDate startDate, LocalDate endDate, LocalDateTime ticketOpenAt, Long hallId) {
        validatePerformance(startDate, endDate, ticketOpenAt);
        this.title = title;
        this.description = description;
        this.runtime = runtime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ticketOpenAt = ticketOpenAt;
        this.hallId = hallId;
    }

    private static void validatePerformance(LocalDate startDate, LocalDate endDate, LocalDateTime ticketOpenAt) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(PerformanceErrorCode.INVALID_PERIOD);
        }
        if (ticketOpenAt != null && ticketOpenAt.toLocalDate().isAfter(startDate)) {
            throw new BusinessException(PerformanceErrorCode.INVALID_TICKET_OPEN);
        }
    }

}
