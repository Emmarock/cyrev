package com.cyrev.iam.entra.service.clients;

import com.cyrev.common.dtos.EntraTokenResponse;
import com.cyrev.iam.config.EntraProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntraTokenClient {

    private final WebClient webClient = WebClient.create();
    private final EntraProperties props;

    public EntraTokenResponse getToken(String tenantId) {
        return getToken(tenantId, "https://graph.microsoft.com/.default");
    }

    public EntraTokenResponse getToken(String tenantId, String scope) {

        String url = props.getAuthority()
                + "/" + tenantId
                + "/oauth2/v2.0/token";

        log.info("Requesting token: tenantId={}, scope={}, clientId={}", tenantId, scope, props.getAppId());

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("client_id", props.getAppId())
                        .with("client_secret", props.getClientSecret())
                        .with("scope", scope)
                        .with("grant_type", "client_credentials"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("OAuth2 token request failed for tenant={}, scope={}: {}", tenantId, scope, errorBody);
                                    return Mono.error(new RuntimeException("Token request failed: " + errorBody));
                                }))
                .bodyToMono(EntraTokenResponse.class)
                .block();
    }

    public EntraTokenResponse getTokenFromCode(String code, String redirectUri) {

        String url = props.getAuthTokenUrl();
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("client_id", props.getAppId())
                        .with("client_secret", props.getClientSecret())
                        .with("code", code)
                        .with("redirect_uri", redirectUri)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Microsoft OAuth Token Error: {}", errorBody);
                                    return Mono.error(new RuntimeException(
                                            "Token request failed: " + errorBody));
                                }))
                .bodyToMono(EntraTokenResponse.class)
                .block();
    }
}