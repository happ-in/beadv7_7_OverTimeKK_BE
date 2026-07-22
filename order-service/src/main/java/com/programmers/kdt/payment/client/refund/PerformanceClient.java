package com.programmers.kdt.payment.client.refund;

import java.time.LocalDate;

public interface PerformanceClient {
    LocalDate getPerformanceEndDate(Long ticketId);
    LocalDate getPerformanceDate(Long ticketId);
}
