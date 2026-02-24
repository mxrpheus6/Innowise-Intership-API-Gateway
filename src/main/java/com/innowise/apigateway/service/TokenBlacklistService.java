package com.innowise.apigateway.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final String PREFIX = "blacklist:jti:";

    public Mono<Boolean> addToBlacklist(String jti, long ttlSeconds) {
        return redisTemplate.opsForValue()
                .set(PREFIX + jti, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    public Mono<Boolean> isBlacklisted(String jti) {
        return redisTemplate.hasKey(PREFIX + jti);
    }

}
