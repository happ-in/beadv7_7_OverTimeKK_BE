package com.programmers.kdt.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

/**
 * User 엔티티/UserRepository에 의존하지 않는 순수 JWT 발급/검증 컴포넌트.
 * order/performance-service에서도 재사용하도록 common으로 이동함.
 * jwt.algorithm 설정에 따라 HS384(대칭키) / RS256(비대칭키)로 동작한다.
 */
@Component
public class JwtProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtKeyMaterial keyMaterial;
    private final long expirationMillis;
    private final long refreshExpirationMillis;

    public JwtProvider(JwtKeyMaterial keyMaterial,
                       @Value("${jwt.expiration-millis}") long expirationMillis,
                       @Value("${jwt.refresh-expiration-millis}") long refreshExpirationMillis) {
        this.keyMaterial = keyMaterial;
        this.expirationMillis = expirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    public String createToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, TYPE_ACCESS, expirationMillis, null);
    }

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
        return sign(builder);
    }

    private String sign(JwtBuilder builder) {
        return switch (keyMaterial.algorithm()) {
            case HS384 -> builder.signWith((SecretKey) keyMaterial.signingKey(), Jwts.SIG.HS384).compact();
            case RS256 -> builder.signWith((PrivateKey) keyMaterial.signingKey(), Jwts.SIG.RS256).compact();
        };
    }

    private Claims parseClaims(String token) {
        return switch (keyMaterial.algorithm()) {
            case HS384 -> Jwts.parser().verifyWith((SecretKey) keyMaterial.verificationKey())
                    .build().parseSignedClaims(token).getPayload();
            case RS256 -> Jwts.parser().verifyWith((PublicKey) keyMaterial.verificationKey())
                    .build().parseSignedClaims(token).getPayload();
        };
    }
}