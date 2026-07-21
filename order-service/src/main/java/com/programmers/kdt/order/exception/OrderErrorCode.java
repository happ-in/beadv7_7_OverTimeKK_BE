package com.programmers.kdt.order.exception;

import com.programmers.kdt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {

    TICKET_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORD400_001", "주문할 티켓 정보가 필요합니다."),
    USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORD400_002", "주문자 ID는 필수입니다."),
    INVALID_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "ORD400_003", "주문 금액은 0보다 커야 합니다."),
    ORDER_ITEMS_REQUIRED(HttpStatus.BAD_REQUEST, "ORD400_004", "주문할 상품이 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD404_001", "주문을 찾을 수 없습니다."),
    ORDER_NOT_PENDING(HttpStatus.CONFLICT, "ORD409_001", "결제 대기 상태 주문만 완료할 수 있습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.CONFLICT, "ORD409_002", "완료된 주문만 취소할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    OrderErrorCode(HttpStatus httpStatus, String code, String message){
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
