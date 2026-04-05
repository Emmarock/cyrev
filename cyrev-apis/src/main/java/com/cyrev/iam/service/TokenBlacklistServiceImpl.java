package com.cyrev.iam.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false", matchIfMissing = true)
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private Cache<String, Long> blacklistCache;

    @Value("${token.blacklist.max-size:100_000}")
    private long maxSize;

    @Value("${token.blacklist.default-ttl-seconds:3600}")
    private long defaultTtlSeconds;

    @PostConstruct
    public void init() {
        blacklistCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfter(new Expiry<String, Long>() {
                    @Override
                    public long expireAfterCreate(String key, Long ttlNanos, long currentTime) {
                        return ttlNanos;
                    }

                    @Override
                    public long expireAfterUpdate(String s, Long aLong, long l, @NonNegative long l1) {
                        return aLong;
                    }

                    @Override
                    public long expireAfterRead(String s, Long aLong, long l, @NonNegative long l1) {
                        return aLong;
                    }
                })
                .build();
    }

    @Override
    public void blacklistToken(String jti, long ttlSeconds) {
        long ttlNanos = TimeUnit.SECONDS.toNanos(ttlSeconds > 0 ? ttlSeconds : defaultTtlSeconds);
        blacklistCache.put(jti, ttlNanos);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return blacklistCache.getIfPresent(jti) != null;
    }
}