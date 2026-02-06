package com.cyrev.iam.adapters.bitbucket;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BitbucketOAuthClient {

    private final WebClient bitbucketWebClient;

    public Mono<BitbucketToken> exchangeCode(String code, String redirectUri) {
        return bitbucketWebClient.post()
            .uri("/site/oauth2/access_token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters
                .fromFormData("grant_type", "authorization_code")
                .with("code", code)
                .with("redirect_uri", redirectUri))
            .retrieve()
            .bodyToMono(BitbucketTokenResponse.class)
            .map(BitbucketTokenResponse::toDomain);
    }

    public Mono<BitbucketToken> refresh(String refreshToken) {
        return bitbucketWebClient.post()
            .uri("/site/oauth2/access_token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters
                .fromFormData("grant_type", "refresh_token")
                .with("refresh_token", refreshToken))
            .retrieve()
            .bodyToMono(BitbucketTokenResponse.class)
            .map(BitbucketTokenResponse::toDomain);
    }
}
