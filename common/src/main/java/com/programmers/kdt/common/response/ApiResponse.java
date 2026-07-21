package com.programmers.kdt.common.response;

import java.util.List;

public record ApiResponse<T>(
        boolean success,
        T data,
        String code,
        String message
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, code, message);
    }

    public static ApiResponse<List<FieldErrorResponse>> failValid(String code, String message, List<FieldErrorResponse> errors) {
        return new ApiResponse<>(false, errors, code, message);
    }
}
