package com.cyrev.iam.adapters.bitbucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class BitbucketTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    private String scopes;

    /**
     * Converts API response → domain token
     */
    public BitbucketToken toDomain() {
        BitbucketToken token = new BitbucketToken();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(
            Instant.now().plusSeconds(expiresIn)
        );
        return token;
    }
}
