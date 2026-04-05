package com.cyrev.iam.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpTokenBlacklistService implements TokenBlacklistService {

    @Override
    public void blacklistToken(String jti, long ttlSeconds) {
        // do nothing
    }

    @Override
    public boolean isBlacklisted(String token) {
        return false;
    }
}