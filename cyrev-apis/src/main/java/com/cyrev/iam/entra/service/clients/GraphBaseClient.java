package com.cyrev.iam.entra.service.clients;

import com.cyrev.iam.entra.service.onboarding.TenantAccessTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GraphBaseClient {

    private final TenantAccessTokenService tokenService;
    private final WebClient.Builder webClientBuilder;

    private static final String GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0";

    public WebClient tenantClient(String tenantId) {

        String accessToken = tokenService.getTenantUserAccessToken(tenantId);

        return webClientBuilder
                .baseUrl(GRAPH_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    public WebClient authClient(String accessToken, String code) {
        return webClientBuilder
                .baseUrl(GRAPH_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}