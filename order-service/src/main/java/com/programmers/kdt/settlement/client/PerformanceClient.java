package com.programmers.kdt.settlement.client;

import com.programmers.kdt.settlement.dto.SettlementSessionResponse;

import java.time.LocalDate;
import java.util.List;

public interface PerformanceClient {
    public List<SettlementSessionResponse> getEndedSessions(LocalDate settlementMonth);


}
