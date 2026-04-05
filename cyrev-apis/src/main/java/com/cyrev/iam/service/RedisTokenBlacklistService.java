package com.cyrev.iam.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final Optional<StringRedisTemplate> redisTemplate;

    private static final String PREFIX = "token_blacklist:";
    public RedisTokenBlacklistService(Optional<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String jti, long ttlSeconds) {
        redisTemplate.get().opsForValue()
                .set(PREFIX + jti, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return redisTemplate.get().hasKey(PREFIX + jti);
    }
}