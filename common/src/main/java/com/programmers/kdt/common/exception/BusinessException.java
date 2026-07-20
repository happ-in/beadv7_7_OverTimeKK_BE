package com.programmers.kdt.common.exception;

import java.text.MessageFormat;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(MessageFormat.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
