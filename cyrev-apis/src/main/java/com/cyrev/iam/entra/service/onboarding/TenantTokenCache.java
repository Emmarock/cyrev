package com.cyrev.iam.entra.service.onboarding;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TenantTokenCache {

    private static class CachedToken {
        String token;
        Instant expiry;
    }

    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public Optional<String> getToken(String tenantId) {

        CachedToken cached = cache.get(tenantId);

        if (cached == null) {
            return Optional.empty();
        }

        if (cached.expiry.isBefore(Instant.now())) {
            cache.remove(tenantId);
            return Optional.empty();
        }

        return Optional.of(cached.token);
    }

    public void storeToken(String tenantId, String token, long expiresIn) {

        CachedToken cached = new CachedToken();
        cached.token = token;
        cached.expiry = Instant.now().plusSeconds(expiresIn - 60);

        cache.put(tenantId, cached);
    }
}