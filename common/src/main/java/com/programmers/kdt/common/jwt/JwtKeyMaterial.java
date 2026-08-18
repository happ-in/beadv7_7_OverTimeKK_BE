package com.programmers.kdt.common.jwt;

import java.security.Key;

/**
 * 서명/검증 키 한 쌍.
 * HS384: signingKey == verificationKey (SecretKey)
 * RS256: signingKey=PrivateKey(발급자만), verificationKey=PublicKey(모든 서비스)
 */
public record JwtKeyMaterial(
        JwtAlgorithm algorithm,
        Key signingKey,
        Key verificationKey
) {
}