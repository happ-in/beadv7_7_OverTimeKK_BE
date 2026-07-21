package com.programmers.kdt.payment.controller;

import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.payment.dto.*;
import com.programmers.kdt.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 요청
    @PostMapping("/pay")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreatePaymentResponse> pay(@RequestBody CreatePaymentRequest request) {
        CreatePaymentResponse response = paymentService.pay(request);
        return ApiResponse.success(response);
    }

    // 결제 승인
    @PostMapping("/{paymentId}/confirm")
    public ApiResponse<ConfirmPaymentResponse> confirm(
            @PathVariable Long paymentId,
            @RequestBody ConfirmPaymentRequest request
    ) {
        ConfirmPaymentResponse response = paymentService.confirm(paymentId, request);
        return ApiResponse.success(response);
    }

    // 결제 실패
    @PostMapping("/{paymentId}/fail")
    public ApiResponse<FailPaymentResponse> fail(
            @PathVariable Long paymentId,
            @RequestBody FailPaymentRequest request
    ) {
        FailPaymentResponse response = paymentService.fail(paymentId, request);
        return ApiResponse.success(response);
    }

    // 결제 내역 조회
    @GetMapping
    public ApiResponse<Page<GetPaymentHistoryResponse>> getPaymentHistory(
            @RequestParam Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<GetPaymentHistoryResponse> response = paymentService.getPaymentHistory(userId, pageable);
        return ApiResponse.success(response);
    }

    // 결제 전액 취소
    @PostMapping("/{paymentId}/cancel")
    public ApiResponse<CancelPaymentResponse> cancel(
            @PathVariable Long paymentId,
            @RequestBody CancelPaymentRequest request) {

        CancelPaymentResponse response = paymentService.cancel(paymentId, request);
        return ApiResponse.success(response);
    }

    // 결제 부분 취소
    @PostMapping("/{paymentId}/partial-cancel")
    public ApiResponse<PartialCancelPaymentResponse> partialCancel(
            @PathVariable Long paymentId,
            @RequestBody PartialCancelPaymentRequest request) {

        PartialCancelPaymentResponse response = paymentService.partialCancel(paymentId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{paymentId}/refund")
    public ApiResponse<Page<GetPaymentRefundHistoryResponse>> getRefundHistory(
            @PathVariable Long paymentId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<GetPaymentRefundHistoryResponse> response = paymentService.getRefundHistory(paymentId, pageable);
        return ApiResponse.success(response);
    }
}
