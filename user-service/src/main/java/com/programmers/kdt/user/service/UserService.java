package com.programmers.kdt.user.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.user.dto.LoginRequest;
import com.programmers.kdt.user.dto.LoginResponse;
import com.programmers.kdt.user.dto.SignUpBusinessRequest;
import com.programmers.kdt.user.dto.SignUpIndividualRequest;
import com.programmers.kdt.user.dto.SignUpUserResponse;
import com.programmers.kdt.user.dto.WithdrawRequest;
import com.programmers.kdt.user.entity.User;
import com.programmers.kdt.user.exception.UserErrorCode;
import com.programmers.kdt.user.jwt.JwtProvider;
import com.programmers.kdt.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인터페이스 없이 클래스 하나로 (팀 요청: MVC 단순화)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignUpUserResponse signUpIndividual(SignUpIndividualRequest request) {
        validateDuplicate(request.username(), request.email());

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.signUpIndividual(request.username(), request.email(), encodedPassword);
        User saved = userRepository.save(user);

        return SignUpUserResponse.from(saved);
    }

    @Transactional
    public SignUpUserResponse signUpBusiness(SignUpBusinessRequest request) {
        validateDuplicate(request.username(), request.email());
        validateDuplicateBusinessNumber(request.businessNumber());

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.signUpBusiness(request.username(), request.email(), encodedPassword,
                request.businessName(), request.businessNumber());
        User saved = userRepository.save(user);

        return SignUpUserResponse.from(saved);
    }

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

    // TODO: 조회 기능 추가
}
