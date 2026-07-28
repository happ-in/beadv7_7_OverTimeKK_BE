package com.programmers.kdt.ticket.exception;

import com.programmers.kdt.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TicketErrorCode implements ErrorCode {
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND,"TKT404_001", "존재하지 않는 티켓입니다. {0}"),
    TICKET_ISSUE_FAILED(HttpStatus.CONFLICT, "TKT409_001", "티켓 발행 중 에러가 발생했습니다."),
    IMPOSSIBLE_HOLD_TICKET(HttpStatus.UNPROCESSABLE_CONTENT, "TKT422_001", "이미 점유된 상태입니다."),
    HOLD_TICKET_NOT_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "TKT422_002", "티켓 점유 만료 시간이 지나지 않았습니다."),
    HOLD_KEY_DISCREPANCY(HttpStatus.UNPROCESSABLE_CONTENT, "TKT422_002", "티켓 점유 만료 시간이 지나지 않았습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
