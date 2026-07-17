package com.programmers.kdt.user.user.dto;

public record SignUpUserResponse(
        Long userId,
        String email,
        String username
) {
}
