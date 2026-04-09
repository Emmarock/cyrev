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

        String url = props.getAuthority()
                + "/" + tenantId
                + "/oauth2/v2.0/token";

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "client_id=" + props.getAppId() +
                        "&client_secret=" + props.getClientSecret() +
                        "&scope=https://graph.microsoft.com/.default" +
                        "&grant_type=client_credentials")
                .retrieve()
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