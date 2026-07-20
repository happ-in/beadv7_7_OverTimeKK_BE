package com.programmers.kdt.performance.exception;
import com.programmers.kdt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PerformanceErrorCode implements ErrorCode {

    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "P400_1", "공연 종료일은 시작일 이후여야 합니다."),
    INVALID_TICKET_OPEN(HttpStatus.BAD_REQUEST, "P400_2", "티켓 오픈 시각은 공연 시작 전이어야 합니다."),
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "P404_1", "공연을 찾을 수 없습니다."),
    NOT_PERFORMANCE_OWNER(HttpStatus.FORBIDDEN, "P403_1", "해당 공연의 판매자가 아닙니다.");

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
