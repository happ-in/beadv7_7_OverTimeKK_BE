package com.programmers.kdt.common.exception;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.common.response.FieldErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<FieldErrorResponse>>> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldErrorResponse> fieldErrorResponses = e.getBindingResult().getFieldErrors()
                .stream().map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage())).toList();

        return ResponseEntity.badRequest().body(ApiResponse.failValid("COM400_002", "입력값이 올바르지 않습니다.", fieldErrorResponses));
    }
}