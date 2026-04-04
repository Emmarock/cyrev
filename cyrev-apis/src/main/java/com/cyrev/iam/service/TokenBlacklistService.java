package com.cyrev.iam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "token_blacklist:";

    public void blacklistToken(String jti, long ttlSeconds) {
        redisTemplate.opsForValue()
                .set(PREFIX + jti, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey(PREFIX + jti);
    }
}