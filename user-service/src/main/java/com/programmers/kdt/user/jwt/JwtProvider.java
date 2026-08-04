package com.programmers.kdt.user.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * User 엔티티/UserRepository에 의존하지 않는 순수 JWT 발급/검증 컴포넌트.
 * 추후 common 모듈로 이동해 order/performance-service에서도 재사용할 것을 염두에 둠.
 */
@Component
public class JwtProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long expirationMillis;
    private final long refreshExpirationMillis;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expiration-millis}") long expirationMillis,
                        @Value("${jwt.refresh-expiration-millis}") long refreshExpirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    public String createToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, TYPE_ACCESS, expirationMillis, null);
    }

    /**
     * refresh token은 role을 담지 않는다 — refresh 시점에 DB에서 최신 사용자 정보를 다시 조회해 role을 반영하므로,
     * 토큰 자체에 실어봤자 신뢰할 값이 아니다.
     * @param tokenId 재발급(로테이션) 시 이전에 발급된 refresh token과 구분하기 위한 식별자(jti). 재사용 탐지에 사용.
     */
    public String createRefreshToken(Long userId, String username, String tokenId) {
        return buildToken(userId, username, null, TYPE_REFRESH, refreshExpirationMillis, tokenId);
    }

    public Long getUserId(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    public String getUsername(String token) {
        return parseClaims(token).get(CLAIM_USERNAME, String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    public String getTokenId(String token) {
        return parseClaims(token).getId();
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    /**
     * @throws JwtException 서명 위조, 만료, 형식 오류 등 토큰이 유효하지 않은 모든 경우
     */
    public void validateToken(String token) {
        parseClaims(token);
    }

    private String buildToken(Long userId, String username, String role, String type, long ttlMillis, String tokenId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ttlMillis);

        JwtBuilder builder = Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_TYPE, type)
                .issuedAt(now)
                .expiration(expiration);
        if (role != null) {
            builder.claim(CLAIM_ROLE, role);
        }
        if (tokenId != null) {
            builder.id(tokenId);
        }
        return builder.signWith(key).compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
