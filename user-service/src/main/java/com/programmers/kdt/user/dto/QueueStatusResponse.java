package com.programmers.kdt.user.dto;

public record QueueStatusResponse(
        String status,
        Long position
) {
}
