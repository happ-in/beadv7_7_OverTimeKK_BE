package com.programmers.kdt.order.settlement.service;

import com.programmers.kdt.order.settlement.repository.SettlementLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementLogRepository settlementLogRepository;

    public void settle(Long perfomanceInfoId) {
        // TODO: performance-service에서 공연완료 이벤트/호출을 받아 정산 생성
    }
}
