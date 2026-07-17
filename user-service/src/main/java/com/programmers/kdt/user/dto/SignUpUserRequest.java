package com.programmers.kdt.user.dto;

public record SignUpUserRequest(
        String email,
        String username,
        String password
) {
}
