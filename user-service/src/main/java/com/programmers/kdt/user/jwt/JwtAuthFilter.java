package com.programmers.kdt.user.jwt;

import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.common.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * Authorization 헤더의 JWT를 검증해 userId를 request attribute로 전달하는 필터.
 * spring-boot-starter-security 미사용 결정에 따라 SecurityFilterChain 대신 서블릿 필터로 직접 구현.
 * 토큰이 없는 요청은 그대로 통과시키고(로그인/회원가입 등 공개 API), 토큰이 있는데 유효하지 않으면 즉시 401로 응답한다.
 * 이번 이슈 범위엔 로그인이 필수인 엔드포인트가 없어 실제로 이 필터를 거쳐 거부되는 API는 아직 없다(추후 보호 API 추가 시 활용).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String USER_ROLE_ATTRIBUTE = "userRole";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                jwtProvider.validateToken(token);
                if (jwtProvider.isRefreshToken(token)) {
                    writeUnauthorized(response);
                    return;
                }
                request.setAttribute(USER_ID_ATTRIBUTE, jwtProvider.getUserId(token));
                request.setAttribute(USER_ROLE_ATTRIBUTE, jwtProvider.getRole(token));
            } catch (JwtException | IllegalArgumentException e) {
                writeUnauthorized(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_TOKEN;
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> body = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());
        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}
