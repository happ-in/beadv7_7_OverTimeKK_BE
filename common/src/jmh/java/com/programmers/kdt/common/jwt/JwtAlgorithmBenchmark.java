package com.programmers.kdt.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.openjdk.jmh.annotations.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * HS384 vs RS256의 순수 서명/검증 CPU 비용 비교.
 * Spring 컨텍스트·BCrypt·DB·네트워크를 모두 배제하고 암호 연산만 격리 측정한다.
 * 실행: ./gradlew :common:jmh
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms512m", "-Xmx512m"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class JwtAlgorithmBenchmark {

    private static final String SECRET = "dev-only-secret-key-please-change-in-real-deployment-32bytes+";
    private static final long TTL_MILLIS = 1_800_000L;

    private SecretKey hsKey;
    private PrivateKey rsaPrivateKey;
    private PublicKey rsaPublicKey;

    private String hsToken;
    private String rsToken;

    @Setup(Level.Trial)
    public void setUp() throws Exception {
        hsKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        rsaPrivateKey = pair.getPrivate();
        rsaPublicKey = pair.getPublic();

        hsToken = hs384Sign();
        rsToken = rs256Sign();
    }

    @Benchmark
    public String hs384Sign() {
        return baseBuilder().signWith(hsKey, Jwts.SIG.HS384).compact();
    }

    @Benchmark
    public String rs256Sign() {
        return baseBuilder().signWith(rsaPrivateKey, Jwts.SIG.RS256).compact();
    }

    @Benchmark
    public Claims hs384Verify() {
        return Jwts.parser().verifyWith(hsKey).build()
                .parseSignedClaims(hsToken).getPayload();
    }

    @Benchmark
    public Claims rs256Verify() {
        return Jwts.parser().verifyWith(rsaPublicKey).build()
                .parseSignedClaims(rsToken).getPayload();
    }

    @Benchmark
    public int tokenSizeHs384() {
        return hsToken.length();
    }

    @Benchmark
    public int tokenSizeRs256() {
        return rsToken.length();
    }

    private io.jsonwebtoken.JwtBuilder baseBuilder() {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", 1L)
                .claim("username", "benchuser")
                .claim("role", "INDIVIDUAL")
                .claim("type", "access")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TTL_MILLIS));
    }
}