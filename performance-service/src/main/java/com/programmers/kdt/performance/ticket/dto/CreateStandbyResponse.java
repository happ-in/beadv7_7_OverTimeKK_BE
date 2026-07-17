package com.programmers.kdt.performance.ticket.dto;

public record CreateStandbyResponse(
        Long standbyId,
        String zone,
        String status
) {
}
