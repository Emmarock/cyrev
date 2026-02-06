package com.cyrev.iam.adapters.bitbucket;

import lombok.Data;

import java.time.Instant;

@Data
public class BitbucketToken {
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt.minusSeconds(60));
    }
}
