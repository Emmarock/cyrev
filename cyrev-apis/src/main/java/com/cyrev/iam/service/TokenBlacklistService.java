package com.cyrev.iam.service;

public interface TokenBlacklistService {
    void blacklistToken(String jti, long ttlSeconds);

    boolean isBlacklisted(String jti);
}
