package com.programmers.kdt.user.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String SECRET = "test-secret-key-for-jwt-provider-unit-test-32bytes+";

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 3600_000L);

    @Test
    void 토큰_발급_후_검증하면_원래_담았던_정보가_그대로_나온다() {
        String token = jwtProvider.createToken(1L, "user1");

        jwtProvider.validateToken(token);
        assertThat(jwtProvider.getUserId(token)).isEqualTo(1L);
        assertThat(jwtProvider.getUsername(token)).isEqualTo("user1");
    }

    @Test
    void 만료된_토큰을_검증하면_예외가_발생한다() {
        JwtProvider expiredTokenProvider = new JwtProvider(SECRET, -1000L);
        String expiredToken = expiredTokenProvider.createToken(1L, "user1");

        assertThatThrownBy(() -> jwtProvider.validateToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void 위조된_토큰을_검증하면_예외가_발생한다() {
        String token = jwtProvider.createToken(1L, "user1");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThatThrownBy(() -> jwtProvider.validateToken(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void 다른_secret으로_서명된_토큰을_검증하면_예외가_발생한다() {
        JwtProvider otherProvider = new JwtProvider("other-secret-key-completely-different-32bytes!!", 3600_000L);
        String token = otherProvider.createToken(1L, "user1");

        assertThatThrownBy(() -> jwtProvider.validateToken(token))
                .isInstanceOf(JwtException.class);
    }
}
