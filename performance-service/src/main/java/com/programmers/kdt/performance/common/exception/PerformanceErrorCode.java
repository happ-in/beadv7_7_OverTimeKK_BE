package com.programmers.kdt.performance.common.exception;

import com.programmers.kdt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PerformanceErrorCode implements ErrorCode {
    PERFORMANCE_NOT_FOUND(HttpStatus.UNPROCESSABLE_CONTENT, "P422_1", "공연 정보를 찾을 수 없습니다."),
    PERFORMANCE_SESSION_NOT_FOUND(HttpStatus.UNPROCESSABLE_CONTENT, "P422_2", "공연 회차 정보를 찾을 수 없습니다."),
    PERFORMANCE_SESSION_NOT_VALID(HttpStatus.UNPROCESSABLE_CONTENT, "P422_10", "공연 정보가 유효하지 않습니다."),
    PERFORMANCE_SESSION_INVALID_START_TIME(HttpStatus.UNPROCESSABLE_CONTENT, "P422_11", "공연 시작 시간은 현재보다 이후여야 합니다."),
    PERFORMANCE_SESSION_UPDATE_NOT_ALLOWED_AFTER_TICKET_OPEN(HttpStatus.UNPROCESSABLE_CONTENT, "P422_12", "공연 티켓 발권 시작 후에는 공연 정보 변경이 불가능합니다.");

    private HttpStatus httpStatus;
    private String code;
    private String message;

    PerformanceErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
