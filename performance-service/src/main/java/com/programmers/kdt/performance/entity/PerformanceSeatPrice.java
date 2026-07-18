package com.programmers.kdt.performance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공연장 좌석 가격 (ERD: Performance_seat_price)
 * Seat에 직접 가격을 두면 같은 홀을 쓰는 모든 공연의 좌석가가 동일해지는 문제가 있어서,
 * 공연(performanceInfoId) + 홀(hallId) + 구역(zone) 조합별로 가격을 따로 매기기 위해 추가된 테이블입니다.
 */
@Entity
@Getter
@NoArgsConstructor
public class PerformanceSeatPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long performanceInfoId;
    private Long hallId;
    private String zone;
    private Long price;
}
