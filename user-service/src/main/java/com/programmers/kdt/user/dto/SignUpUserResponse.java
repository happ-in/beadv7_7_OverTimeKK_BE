package com.programmers.kdt.user.dto;

import com.programmers.kdt.user.entity.User;

public record SignUpUserResponse(
        Long userId,
        String email,
        String username
) {
    public static SignUpUserResponse from(User user) {
        return new SignUpUserResponse(user.getUserId(), user.getEmail(), user.getUsername());
    }
}
