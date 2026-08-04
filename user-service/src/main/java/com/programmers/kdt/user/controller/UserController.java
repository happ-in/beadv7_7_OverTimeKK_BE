package com.programmers.kdt.user.controller;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.common.response.ApiResponse;
import com.programmers.kdt.user.dto.LoginRequest;
import com.programmers.kdt.user.dto.LoginResponse;
import com.programmers.kdt.user.dto.RefreshTokenRequest;
import com.programmers.kdt.user.dto.SignUpBusinessRequest;
import com.programmers.kdt.user.dto.SignUpIndividualRequest;
import com.programmers.kdt.user.dto.UserResponse;
import com.programmers.kdt.user.dto.WithdrawRequest;
import com.programmers.kdt.user.jwt.JwtAuthFilter;
import com.programmers.kdt.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup/individual")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signUpIndividual(@Valid @RequestBody SignUpIndividualRequest request) {
        return ApiResponse.success(userService.signUpIndividual(request));
    }

    @PostMapping("/signup/business")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signUpBusiness(@Valid @RequestBody SignUpBusinessRequest request) {
        return ApiResponse.success(userService.signUpBusiness(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @PostMapping("/token/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(userService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestAttribute(value = JwtAuthFilter.USER_ID_ATTRIBUTE, required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        userService.logout(userId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@RequestAttribute(value = JwtAuthFilter.USER_ID_ATTRIBUTE, required = false) Long userId,
                                       @Valid @RequestBody WithdrawRequest request) {
        if (userId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        userService.withdraw(userId, request);
        return ApiResponse.success(null);
    }

    /**
     * 로그인한 본인의 정보 조회. 프론트는 반드시 이 API만 사용 (아래 /{userId}를 직접 호출 금지)
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@RequestAttribute(value = JwtAuthFilter.USER_ID_ATTRIBUTE, required = false) Long userId) {
        if (userId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(userService.getUser(userId));
    }

    /**
     * 다른 서비스(order-service 등)가 REST로 회원 존재 여부를 확인할 때 쓰는 내부용 API
     * 프론트가 직접 호출하면 안 됨 (인증 없이 임의 userId 조회 가능 — 서비스 간 통신 전용)
     * TODO: 실제로는 이런 서비스 간 호출 엔드포인트를 명확히 구분(예: /internal/**)하는 것을 고려
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.success(userService.getUser(userId));
    }
}
