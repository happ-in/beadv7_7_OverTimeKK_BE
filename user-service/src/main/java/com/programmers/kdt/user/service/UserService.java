package com.programmers.kdt.user.service;

import com.programmers.kdt.user.dto.SignUpUserRequest;
import com.programmers.kdt.user.dto.SignUpUserResponse;
import com.programmers.kdt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인터페이스 없이 클래스 하나로 (팀 요청: MVC 단순화)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public SignUpUserResponse signUp(SignUpUserRequest request) {
        // TODO: 이메일 중복 체크, 비밀번호 암호화 등
        return null;
    }

    // TODO: 로그인, 탈퇴, 조회 기능 추가
}
