package com.programmers.kdt.user.dto;

import com.programmers.kdt.user.entity.Role;
import com.programmers.kdt.user.entity.User;
import com.programmers.kdt.user.entity.UserStatus;

public record UserResponse(
        Long userId,
        String email,
        String username,
        UserStatus status,
        Role userType,
        boolean emailVerified
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getEmail(), user.getUsername(), user.getStatus(), user.getUserType(), user.isEmailVerified());
    }
}
