package com.programmers.kdt.common.jwt;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Configuration
public class JwtKeyConfig {

    @Bean
    public JwtKeyMaterial jwtKeyMaterial(
            @Value("${jwt.algorithm:HS384}") JwtAlgorithm algorithm,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.rsa.key-size:2048}") int rsaKeySize) {

        return switch (algorithm) {
            case HS384 -> hs384(secret);
            case RS256 -> rs256(rsaKeySize);
        };
    }

    private JwtKeyMaterial hs384(String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return new JwtKeyMaterial(JwtAlgorithm.HS384, key, key);
    }

    private JwtKeyMaterial rs256(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            KeyPair pair = generator.generateKeyPair();
            return new JwtKeyMaterial(JwtAlgorithm.RS256, pair.getPrivate(), pair.getPublic());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA 키 쌍 생성 실패", e);
        }
    }
}