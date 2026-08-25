package com.programmers.kdt.user.dto;

public record QueueEnterResponse(
        String status,
        String token,
        Long position
) {
}
