package com.programmers.kdt.settlement.exception;

import com.programmers.kdt.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum SettlementErrorCode implements ErrorCode {

    SELLER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "SET400_001", "판매자 ID는 필수입니다."),
    SETTLEMENT_MONTH_REQUIRED(HttpStatus.BAD_REQUEST, "SET400_002", "정산 대상 월은 필수입니다."),
    SCHEDULED_SETTLEMENT_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "SET400_003", "정산 예정일은 필수입니다."),
    INVALID_SETTLEMENT_MONTH(HttpStatus.BAD_REQUEST, "SET400_004", "정산 대상 월은 해당 월의 1일이어야 합니다."),
    INVALID_SETTLEMENT_AMOUNT(HttpStatus.BAD_REQUEST, "SET400_005", "총 판매금액과 서비스 수수료는 0원 이상이어야 합니다."),
    SERVICE_FEE_EXCEEDS_GROSS_AMOUNT(HttpStatus.BAD_REQUEST, "SET400_006", "서비스 수수료는 총 판매금액보다 클 수 없습니다."),
    PAID_AT_REQUIRED(HttpStatus.BAD_REQUEST, "SET400_007", "지급 완료 시각은 필수입니다."),
    INVALID_SETTLEMENT_STATUS(HttpStatus.CONFLICT, "SET409_001", "현재 정산 상태에서는 요청한 상태로 변경할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SettlementErrorCode(
            HttpStatus httpStatus,
            String code,
            String message
    ) {
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