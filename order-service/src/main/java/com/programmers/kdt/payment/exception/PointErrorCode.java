package com.programmers.kdt.payment.exception;

import com.programmers.kdt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PointErrorCode implements ErrorCode {

    MISSING_USER_ID(HttpStatus.BAD_REQUEST, "PT_400_1", "사용자 정보가 없습니다."),
    ZERO_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "PT_400_2", "포인트 금액은 0원보다 커야 합니다. 금액: {0}"),
    MISSING_EVENT_ID(HttpStatus.BAD_REQUEST, "PT_400_3", "eventId가 없습니다."),
    INVALID_ROLLBACK_TARGET(HttpStatus.BAD_REQUEST, "PT_400_4", "환급은 사용(USE) 로그에 대해서만 가능합니다."),
    ROLLBACK_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "PT_400_5", "취소 금액이 원본 사용 금액을 초과했습니다. 원본: {0}, 취소: {1}"),

    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "PT_409_1", "보유 포인트가 부족합니다. 보유: {0}, 요청: {1}")
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PointErrorCode(HttpStatus httpStatus, String code, String message) {
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