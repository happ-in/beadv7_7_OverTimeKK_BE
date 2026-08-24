package com.programmers.kdt.filter;

import com.programmers.kdt.common.exception.CommonErrorCode;
import com.programmers.kdt.common.jwt.JwtProvider;
import com.programmers.kdt.common.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * 게이트웨이 진입점에서 JWT를 검증하고, 검증된 userId만 X-User-Id 헤더로 다운스트림에 전달
 * 클라이언트가 직접 보낸 X-User-Id는 위조 방지를 위해 항상 먼저 제거
 * 토큰이 없으면 그대로 통과(공개 API 허용), 토큰이 있는데 유효하지 않으면 즉시 401로 응답
 * (common의 JwtAuthFilter와 동일한 검증 규칙 - 검증 지점만 게이트웨이로 옮긴 것)
 */
public class JwtAuthFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    public static final String USER_ID_HEADER = "X-User-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public JwtAuthFilterFunction(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        String token = resolveToken(request);
        Long verifiedUserId = null;

        if (token != null) {
            try {
                jwtProvider.validateToken(token);
                if (jwtProvider.isRefreshToken(token)) {
                    return unauthorized();
                }

                Long userId = jwtProvider.getUserId(token);
                if (userId == null || userId <= 0) {
                    return unauthorized();
                }
                String role = jwtProvider.getRole(token);
                if (!StringUtils.hasText(role)) {
                    return unauthorized();
                }

                verifiedUserId = userId;
            } catch (JwtException | IllegalArgumentException e) {
                return unauthorized();
            }
        }

        Long finalUserId = verifiedUserId;
        ServerRequest forwardRequest = ServerRequest.from(request)
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    if (finalUserId != null) {
                        headers.set(USER_ID_HEADER, String.valueOf(finalUserId));
                    }
                })
                .build();

        return next.handle(forwardRequest);
    }

    private String resolveToken(ServerRequest request) {
        String header = request.headers().firstHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private ServerResponse unauthorized() {
        CommonErrorCode errorCode = CommonErrorCode.INVALID_TOKEN;
        ApiResponse<Void> body = ApiResponse.fail(errorCode.getCode(), errorCode.getMessage());
        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonMapper.writeValueAsString(body));
    }
}
