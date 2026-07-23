package com.programmers.kdt.user.service;

import com.programmers.kdt.common.exception.BusinessException;
import com.programmers.kdt.user.dto.SignUpBusinessRequest;
import com.programmers.kdt.user.dto.SignUpUserResponse;
import com.programmers.kdt.user.entity.User;
import com.programmers.kdt.user.exception.UserErrorCode;
import com.programmers.kdt.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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

}
