package com.programmers.kdt.ticket.exception;

import com.programmers.kdt.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TicketErrorCode implements ErrorCode {
    TICKET_ISSUE_FAILED(HttpStatus.CONFLICT, "TKT409_001", "티켓 발행 중 에러가 발생했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
