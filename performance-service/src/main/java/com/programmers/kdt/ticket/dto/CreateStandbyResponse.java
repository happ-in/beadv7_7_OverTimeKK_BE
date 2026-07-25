package com.programmers.kdt.ticket.dto;

public record CreateStandbyResponse(
        Long standbyId,
        String zone,
        String status
) {
}