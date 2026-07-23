package com.programmers.kdt.venue.exception;

import com.programmers.kdt.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VenueException implements ErrorCode {
    SEAT_INFORMATION_NOT_FOUND(HttpStatus.NOT_FOUND, "VEN404_001", "{0} 홀에 대한 좌석 정보가 없습니다."),
    HALL_INFORMATION_NOT_FOUND(HttpStatus.NOT_FOUND, "VEN404_002", "홀에 대한 정보가 없습니다.");

    public HttpStatus httpStatus;
    public String code;
    public String message;

}
