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

    private static final String GRAPH_AUDIENCE = "graph";

    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public Optional<String> getToken(String tenantId) {
        return getToken(tenantId, GRAPH_AUDIENCE);
    }

    public Optional<String> getToken(String tenantId, String audience) {

        CachedToken cached = cache.get(key(tenantId, audience));

        if (cached == null) {
            return Optional.empty();
        }

        if (cached.expiry.isBefore(Instant.now())) {
            cache.remove(key(tenantId, audience));
            return Optional.empty();
        }

        return Optional.of(cached.token);
    }

    public void storeToken(String tenantId, String token, long expiresIn) {
        storeToken(tenantId, GRAPH_AUDIENCE, token, expiresIn);
    }

    public void storeToken(String tenantId, String audience, String token, long expiresIn) {

        CachedToken cached = new CachedToken();
        cached.token = token;
        cached.expiry = Instant.now().plusSeconds(expiresIn - 60);

        cache.put(key(tenantId, audience), cached);
    }

    private static String key(String tenantId, String audience) {
        return tenantId + "|" + audience;
    }
}