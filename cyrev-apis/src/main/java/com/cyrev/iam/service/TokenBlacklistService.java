package com.cyrev.iam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class TokenBlacklistService {

    private final Optional<StringRedisTemplate> redisTemplate;

    private static final String PREFIX = "token_blacklist:";
    public TokenBlacklistService(Optional<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String jti, long ttlSeconds) {
        redisTemplate.get().opsForValue()
                .set(PREFIX + jti, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    public boolean isBlacklisted(String jti) {
        return redisTemplate.get().hasKey(PREFIX + jti);
    }
}