package com.programmers.kdt.user.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.user.dto.LoginRequest;
import com.programmers.kdt.user.dto.LoginResponse;
import com.programmers.kdt.user.dto.SignUpBusinessRequest;
import com.programmers.kdt.user.dto.SignUpUserResponse;
import com.programmers.kdt.user.dto.WithdrawRequest;
import com.programmers.kdt.user.entity.User;
import com.programmers.kdt.user.entity.UserStatus;
import com.programmers.kdt.user.exception.UserErrorCode;
import com.programmers.kdt.user.jwt.JwtProvider;
import com.programmers.kdt.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserService userService;

    @Test
    void 기업_회원가입_성공() {
        SignUpBusinessRequest request = new SignUpBusinessRequest(
                "business1", "business1@example.com", "password123",
                "회사이름", "123-45-67890");
        given(userRepository.existsByUsername(request.username())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.existsByBusinessNumber(request.businessNumber())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

        User saved = User.signUpBusiness(request.username(), request.email(), "encodedPassword",
                request.businessName(), request.businessNumber());
        ReflectionTestUtils.setField(saved, "userId", 1L);
        given(userRepository.save(any(User.class))).willReturn(saved);

        SignUpUserResponse response = userService.signUpBusiness(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("business1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 중복된_사업자번호로_기업_회원가입시_예외() {
        SignUpBusinessRequest request = new SignUpBusinessRequest(
                "business1", "business1@example.com", "password123",
                "회사이름", "123-45-67890");
        given(userRepository.existsByUsername(request.username())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.existsByBusinessNumber(request.businessNumber())).willReturn(true);

        assertThatThrownBy(() -> userService.signUpBusiness(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.DUPLICATE_BUSINESS_NUMBER.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void 로그인_성공() {
        LoginRequest request = new LoginRequest("user1", "password123");
        User user = User.signUpIndividual("user1", "user1@example.com", "encodedPassword");
        ReflectionTestUtils.setField(user, "userId", 1L);

        given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
        given(jwtProvider.createToken(user.getUserId(), user.getUsername())).willReturn("issued-token");

        LoginResponse response = userService.login(request);

        assertThat(response.accessToken()).isEqualTo("issued-token");
    }

    @Test
    void 존재하지_않는_아이디로_로그인시_예외() {
        LoginRequest request = new LoginRequest("noSuchUser", "password123");
        given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 비밀번호_불일치로_로그인시_예외() {
        LoginRequest request = new LoginRequest("user1", "wrongPassword");
        User user = User.signUpIndividual("user1", "user1@example.com", "encodedPassword");

        given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.INVALID_PASSWORD.getMessage());
    }

    @Test
    void 탈퇴한_계정으로_로그인시_예외() {
        LoginRequest request = new LoginRequest("user1", "password123");
        User user = User.signUpIndividual("user1", "user1@example.com", "encodedPassword");
        ReflectionTestUtils.setField(user, "status", UserStatus.WITHDRAWN);

        given(userRepository.findByUsername(request.username())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.WITHDRAWN_USER.getMessage());
    }

    @Test
    void 회원_탈퇴_성공() {
        WithdrawRequest request = new WithdrawRequest("password123");
        User user = User.signUpIndividual("user1", "user1@example.com", "encodedPassword");
        ReflectionTestUtils.setField(user, "userId", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);

        userService.withdraw(1L, request);

        assertThat(user.isWithdrawn()).isTrue();
    }

    @Test
    void 존재하지_않는_유저_탈퇴시_예외() {
        WithdrawRequest request = new WithdrawRequest("password123");
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.withdraw(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 이미_탈퇴한_계정_재탈퇴시_예외() {
        WithdrawRequest request = new WithdrawRequest("password123");
        User user = User.signUpIndividual("user1", "user1@example.com", "encodedPassword");
        ReflectionTestUtils.setField(user, "userId", 1L);
        ReflectionTestUtils.setField(user, "status", UserStatus.WITHDRAWN);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.withdraw(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.WITHDRAWN_USER.getMessage());
    }

    @Test
    void 비밀번호_불일치로_탈퇴시_예외() {
        WithdrawRequest request = new WithdrawRequest("wrongPassword");
        User user = User.signUpIndividual("user1", "user1@example.com", "encodedPassword");
        ReflectionTestUtils.setField(user, "userId", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> userService.withdraw(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(UserErrorCode.INVALID_PASSWORD.getMessage());
    }

}
