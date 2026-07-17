package com.programmers.kdt.user.user.controller;

import com.programmers.kdt.user.user.dto.SignUpUserRequest;
import com.programmers.kdt.user.user.dto.SignUpUserResponse;
import com.programmers.kdt.user.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public SignUpUserResponse signUp(@Valid @RequestBody SignUpUserRequest request) {
        return userService.signUp(request);
    }

    /**
     * 다른 서비스(order-service 등)가 REST로 회원 존재 여부를 확인할 때 쓰는 내부용 API
     * TODO: 실제로는 이런 서비스 간 호출 엔드포인트를 명확히 구분(예: /internal/**)하는 것을 고려
     */
    @GetMapping("/{userId}")
    public SignUpUserResponse getUser(@PathVariable Long userId) {
        // TODO
        return null;
    }
}
