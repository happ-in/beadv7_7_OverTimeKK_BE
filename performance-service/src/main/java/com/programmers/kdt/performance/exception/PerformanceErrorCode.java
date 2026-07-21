package com.programmers.kdt.performance.exception;
import com.programmers.kdt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PerformanceErrorCode implements ErrorCode {

    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "PFM400_001", "공연 종료일은 시작일 이후여야 합니다."),
    INVALID_TICKET_OPEN(HttpStatus.BAD_REQUEST, "PFM400_002", "티켓 오픈 시각은 공연 시작 전이어야 합니다."),
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PFM404_001", "공연을 찾을 수 없습니다."),
    PERFORMANCE_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "P404_002", "공연 회차 정보를 찾을 수 없습니다."),
    NOT_PERFORMANCE_OWNER(HttpStatus.FORBIDDEN, "PFM403_001", "해당 공연의 판매자가 아닙니다."),
    PERFORMANCE_SESSION_NOT_VALID(HttpStatus.UNPROCESSABLE_CONTENT, "P422_001", "공연 정보가 유효하지 않습니다."),
    PERFORMANCE_SESSION_INVALID_START_TIME(HttpStatus.UNPROCESSABLE_CONTENT, "P422_002", "공연 시작 시간은 현재보다 이후여야 합니다."),
    PERFORMANCE_SESSION_UPDATE_NOT_ALLOWED_AFTER_TICKET_OPEN(HttpStatus.UNPROCESSABLE_CONTENT, "P422_003", "공연 티켓 발권 시작 후에는 회차 정보 {0} 불가 합니다."),
    PERFORMANCE_SESSION_UPDATE_NOT_ALLOWED_AFTER_PERFORMANCE_START(HttpStatus.UNPROCESSABLE_CONTENT, "P422_004", "공연 시작 후에는 회차 정보 {0} 불가 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

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
