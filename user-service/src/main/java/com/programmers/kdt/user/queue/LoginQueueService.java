package com.programmers.kdt.user.queue;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * DB 커넥션 풀(10개)보다 로그인 요청이 몰릴 때, 요청을 DB까지 밀어넣지 않고
 * Redis 단에서 먼저 걸러서 정원(maxConcurrent)만큼만 동시 처리되게 하는 대기열.
 * 여유가 있으면 즉시 통과시키고, 없으면 줄을 세운 뒤 0.5초마다 빈 자리만큼 승격시킨다.
 *
 * "처리 중 인원"은 별도 카운터로 세지 않고, login:active ZSET(member=token, score=만료시각)의
 * "아직 만료 안 된 멤버 수"로 계산한다. 대기열(login:queue) 항목도 동일하게 score를 "만료시각"
 * (진입시각+ttl)으로 등록해서, 승격되지 못한 채 포기된(클라이언트 이탈/타임아웃) 항목이 영원히
 * 쌓이지 않고 promoteQueue()가 주기적으로 청소하도록 한다. 반납(release) 호출을 놓쳐도,
 * 대기 중 포기해도 만료시각이 지나면 자동으로 카운트/대기열에서 빠지기 때문에, 이런 누락이
 * 대기열을 영구적으로 막는 문제로 이어지지 않는다.
 */
@Component
public class LoginQueueService {

    private static final String ACTIVE_KEY = "login:active";
    private static final String QUEUE_KEY = "login:queue";

    /**
     * 만료 안 된 활성 인원 수가 정원 미만이면 즉시 활성 ZSET에 등록(만료시각=now+ttl)하고,
     * 아니면 대기열 ZSET에도 동일하게 만료시각을 score로 등록한다. 조회와 등록을 하나로 묶어
     * 동시 요청이 정원을 초과해 통과하는 레이스를 막는다.
     */
    private static final RedisScript<Long> ENTER_SCRIPT = new DefaultRedisScript<>("""
            local now = tonumber(ARGV[2])
            local active_count = redis.call('ZCOUNT', KEYS[1], now, '+inf')
            local max = tonumber(ARGV[3])
            local expiry = now + tonumber(ARGV[4])
            if active_count < max then
                redis.call('ZADD', KEYS[1], expiry, ARGV[1])
                return 1
            else
                redis.call('ZADD', KEYS[2], expiry, ARGV[1])
                return 0
            end
            """, Long.class);

    /**
     * 만료된 활성 멤버와, 승격되지 못한 채 만료된 대기열 항목(이탈/타임아웃)을 먼저 청소한 뒤,
     * 남은 빈 자리 수만큼 대기열에서 가장 오래 기다린(score가 가장 작은) 순서대로 꺼내
     * 활성 ZSET으로 옮긴다. 대기열 score도 만료시각 기준이라 ttl이 동일한 이상 진입 순서와
     * 만료 순서가 같아 FIFO 순서는 그대로 유지된다.
     */
    private static final RedisScript<List> PROMOTE_SCRIPT = new DefaultRedisScript<>("""
            local now = tonumber(ARGV[2])
            redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', now)
            redis.call('ZREMRANGEBYSCORE', KEYS[2], '-inf', now)
            local active_count = redis.call('ZCARD', KEYS[1])
            local capacity = tonumber(ARGV[1]) - active_count
            if capacity <= 0 then
                return {}
            end
            local promoted = redis.call('ZPOPMIN', KEYS[2], capacity)
            local expiry = now + tonumber(ARGV[3])
            for i = 1, #promoted, 2 do
                redis.call('ZADD', KEYS[1], expiry, promoted[i])
            end
            return promoted
            """, List.class);

    private final StringRedisTemplate redisTemplate;
    private final int maxConcurrent;
    private final long admissionTtlMillis;

    public LoginQueueService(StringRedisTemplate redisTemplate,
                              MeterRegistry meterRegistry,
                              @Value("${login.queue.max-concurrent}") int maxConcurrent,
                              @Value("${login.queue.admission-ttl-seconds}") long admissionTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxConcurrent = maxConcurrent;
        this.admissionTtlMillis = admissionTtlSeconds * 1000;

        Gauge.builder("login.queue.size", this, LoginQueueService::queueSize)
                .description("현재 로그인 대기열에서 기다리고 있는 인원 수")
                .register(meterRegistry);
        Gauge.builder("login.active.count", this, LoginQueueService::activeCount)
                .description("현재 입장 허가되어 로그인 처리 중인 인원 수")
                .register(meterRegistry);
    }

    private double queueSize() {
        Long size = redisTemplate.opsForZSet().size(QUEUE_KEY);
        return size == null ? 0 : size;
    }

    private double activeCount() {
        Long count = redisTemplate.opsForZSet().count(ACTIVE_KEY, now(), Double.POSITIVE_INFINITY);
        return count == null ? 0 : count;
    }

    /**
     * 새 토큰을 발급해 대기열 진입을 시도한다. 여유가 있으면 즉시 READY로 반환된다.
     */
    public QueueEnterResult enter() {
        String token = UUID.randomUUID().toString();
        Long result = redisTemplate.execute(ENTER_SCRIPT,
                List.of(ACTIVE_KEY, QUEUE_KEY),
                token, String.valueOf(now()), String.valueOf(maxConcurrent), String.valueOf(admissionTtlMillis));

        boolean admitted = result != null && result == 1L;
        return new QueueEnterResult(token, admitted);
    }

    /**
     * 토큰의 현재 상태를 조회한다. READY(입장 허가), WAITING(대기 중, 순번 포함), EXPIRED(만료/미존재) 중 하나.
     */
    public QueueStatusResult status(String token) {
        if (isAdmitted(token)) {
            return QueueStatusResult.ready();
        }

        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, token);
        if (rank != null) {
            return QueueStatusResult.waiting(rank + 1);
        }

        return QueueStatusResult.expired();
    }

    /**
     * 입장권으로 로그인 처리를 마친 뒤(성공/실패 무관) 자리를 반납한다.
     * 반납을 놓쳐도(호출 안 돼도) 만료시각이 지나면 활성 카운트에서 자동으로 빠진다.
     */
    public void release(String token) {
        redisTemplate.opsForZSet().remove(ACTIVE_KEY, token);
    }

    /**
     * 입장권이 유효한지(대기열을 거쳐 입장 허가되었고, 아직 만료 전인지) 확인한다.
     */
    public boolean isAdmitted(String token) {
        Double expiry = redisTemplate.opsForZSet().score(ACTIVE_KEY, token);
        return expiry != null && expiry > now();
    }

    @Scheduled(fixedDelay = 500)
    public void promoteQueue() {
        redisTemplate.execute(PROMOTE_SCRIPT,
                List.of(ACTIVE_KEY, QUEUE_KEY),
                String.valueOf(maxConcurrent), String.valueOf(now()), String.valueOf(admissionTtlMillis));
    }

    private long now() {
        return System.currentTimeMillis();
    }

    public record QueueEnterResult(String token, boolean admitted) {
    }

    public record QueueStatusResult(String status, Long position) {
        static QueueStatusResult ready() {
            return new QueueStatusResult("READY", null);
        }

        static QueueStatusResult waiting(long position) {
            return new QueueStatusResult("WAITING", position);
        }

        static QueueStatusResult expired() {
            return new QueueStatusResult("EXPIRED", null);
        }
    }
}
