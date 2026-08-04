package com.programmers.kdt.user.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.user.dto.LoginRequest;
import com.programmers.kdt.user.dto.LoginResponse;
import com.programmers.kdt.user.dto.SignUpBusinessRequest;
import com.programmers.kdt.user.dto.SignUpIndividualRequest;
import com.programmers.kdt.user.dto.UserResponse;
import com.programmers.kdt.user.dto.WithdrawRequest;
import com.programmers.kdt.user.entity.User;
import com.programmers.kdt.user.exception.UserErrorCode;
import com.programmers.kdt.user.jwt.JwtProvider;
import com.programmers.kdt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인터페이스 없이 클래스 하나로 (팀 요청: MVC 단순화).
 * 모킹/테스트 대체 용이성 이슈 없음 — 의존 대상(UserRepository/PasswordEncoder는 인터페이스, JwtProvider는 non-final 클래스)이라
 * 인터페이스 없이도 Mockito로 그대로 모킹 가능함을 확인함.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponse signUpIndividual(SignUpIndividualRequest request) {
        validateDuplicate(request.username(), request.email());

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.signUpIndividual(request.username(), request.email(), encodedPassword);
        User saved = saveUser(user);

        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse signUpBusiness(SignUpBusinessRequest request) {
        validateDuplicate(request.username(), request.email());
        validateDuplicateBusinessNumber(request.businessNumber());

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.signUpBusiness(request.username(), request.email(), encodedPassword,
                request.businessName(), request.businessNumber());
        User saved = saveUser(user);

        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }
        if (user.isWithdrawn()) {
            throw new BusinessException(UserErrorCode.WITHDRAWN_USER);
        }

        String accessToken = jwtProvider.createToken(user.getUserId(), user.getUsername());
        return new LoginResponse(accessToken);
    }

    /**
     * validateDuplicate()의 exists-then-save 사이엔 레이스가 있을 수 있어, 저장 시 유니크 제약 위반이 나면
     * 어느 필드가 충돌했는지 재조회해 기존 BusinessException으로 변환한다.
     */
    private User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new BusinessException(UserErrorCode.DUPLICATE_USERNAME);
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
            }
            if (user.getBusinessNumber() != null && userRepository.existsByBusinessNumber(user.getBusinessNumber())) {
                throw new BusinessException(UserErrorCode.DUPLICATE_BUSINESS_NUMBER);
            }
            throw e;
        }
    }

    private void validateDuplicate(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateDuplicateBusinessNumber(String businessNumber) {
        if (userRepository.existsByBusinessNumber(businessNumber)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }
    }

    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.isWithdrawn()) {
            throw new BusinessException(UserErrorCode.WITHDRAWN_USER);
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }

        user.withdraw();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }
}
