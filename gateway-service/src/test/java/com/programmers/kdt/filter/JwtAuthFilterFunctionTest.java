package com.programmers.kdt.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.programmers.kdt.common.jwt.JwtProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@DisplayName("게이트웨이 JWT 검증 필터")
class JwtAuthFilterFunctionTest {

    private static final String SECRET = "dev-only-secret-key-please-change-in-real-deployment-32bytes+";
    private static final List<HttpMessageConverter<?>> CONVERTERS =
            List.of(new StringHttpMessageConverter(), new JacksonJsonHttpMessageConverter());

    private JwtProvider jwtProvider;
    private JwtAuthFilterFunction filterFunction;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, 1_800_000L, 604_800_000L);
        filterFunction = new JwtAuthFilterFunction(jwtProvider);
    }

    private ServerRequest serverRequestWithHeaders(String authorizationHeader, String spoofedUserIdHeader) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("DELETE", "/api/performances/1");
        if (authorizationHeader != null) {
            mockRequest.addHeader("Authorization", authorizationHeader);
        }
        if (spoofedUserIdHeader != null) {
            mockRequest.addHeader("X-User-Id", spoofedUserIdHeader);
        }
        return ServerRequest.create(mockRequest, CONVERTERS);
    }

    /** next 핸들러를 대신해서, 실제로 다운스트림에 전달된 요청을 그대로 캡처하는 테스트 대역. */
    private static final class CapturingHandler {
        private ServerRequest capturedRequest;
        private boolean invoked = false;

        ServerResponse handle(ServerRequest request) {
            this.invoked = true;
            this.capturedRequest = request;
            return ServerResponse.ok().build();
        }
    }

    @Nested
    @DisplayName("유효한 access token이 있으면")
    class WithValidAccessToken {

        @Test
        @DisplayName("검증된 userId를 X-User-Id 헤더로 주입해서 다음 핸들러로 넘긴다")
        void injectsVerifiedUserId() throws Exception {
            String token = jwtProvider.createToken(777L, "owner777", "SELLER");
            ServerRequest request = serverRequestWithHeaders("Bearer " + token, null);
            CapturingHandler next = new CapturingHandler();

            filterFunction.filter(request, next::handle);

            assertThat(next.invoked).isTrue();
            assertThat(next.capturedRequest.headers().firstHeader("X-User-Id")).isEqualTo("777");
        }

        @Test
        @DisplayName("클라이언트가 같이 보낸 X-User-Id 스푸핑 값은 검증된 값으로 덮어써진다")
        void overridesSpoofedHeaderWithVerifiedUserId() throws Exception {
            String attackerToken = jwtProvider.createToken(888L, "attacker888", "SELLER");
            // 공격자가 진짜 소유자(777)를 사칭하는 X-User-Id를 같이 보냄
            ServerRequest request = serverRequestWithHeaders("Bearer " + attackerToken, "777");
            CapturingHandler next = new CapturingHandler();

            filterFunction.filter(request, next::handle);

            assertThat(next.invoked).isTrue();
            // 사칭한 777이 아니라, 토큰으로 검증된 진짜 888이 최종적으로 전달돼야 한다
            assertThat(next.capturedRequest.headers().firstHeader("X-User-Id")).isEqualTo("888");
        }
    }

    @Nested
    @DisplayName("토큰이 아예 없으면")
    class WithoutToken {

        @Test
        @DisplayName("공개 API를 위해 통과시키되, 클라이언트가 보낸 X-User-Id는 반드시 제거한다")
        void stripsSpoofedHeaderAndPassesThrough() throws Exception {
            ServerRequest request = serverRequestWithHeaders(null, "999");
            CapturingHandler next = new CapturingHandler();

            filterFunction.filter(request, next::handle);

            assertThat(next.invoked).isTrue();
            assertThat(next.capturedRequest.headers().firstHeader("X-User-Id")).isNull();
        }
    }

    @Nested
    @DisplayName("유효하지 않은 토큰이면")
    class WithInvalidToken {

        @Test
        @DisplayName("깨진 토큰이면 즉시 401을 반환하고 다음 핸들러를 호출하지 않는다")
        void rejectsMalformedToken() throws Exception {
            ServerRequest request = serverRequestWithHeaders("Bearer garbage.invalid.token", null);
            CapturingHandler next = new CapturingHandler();

            ServerResponse response = filterFunction.filter(request, next::handle);

            assertThat(next.invoked).isFalse();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("refresh token으로는 인증할 수 없다")
        void rejectsRefreshToken() throws Exception {
            String refreshToken = jwtProvider.createRefreshToken(777L, "owner777", "token-id-1");
            ServerRequest request = serverRequestWithHeaders("Bearer " + refreshToken, null);
            CapturingHandler next = new CapturingHandler();

            ServerResponse response = filterFunction.filter(request, next::handle);

            assertThat(next.invoked).isFalse();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("만료된 토큰은 401로 거부된다")
        void rejectsExpiredToken() throws Exception {
            JwtProvider shortLivedProvider = new JwtProvider(SECRET, 1L, 604_800_000L);
            String expiredToken = shortLivedProvider.createToken(777L, "owner777", "SELLER");
            Thread.sleep(10);
            ServerRequest request = serverRequestWithHeaders("Bearer " + expiredToken, null);
            CapturingHandler next = new CapturingHandler();

            ServerResponse response = filterFunction.filter(request, next::handle);

            assertThat(next.invoked).isFalse();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
