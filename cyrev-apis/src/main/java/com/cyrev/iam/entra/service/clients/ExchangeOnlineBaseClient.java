package com.cyrev.iam.entra.service.clients;

import com.cyrev.iam.entra.service.onboarding.TenantAccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeOnlineBaseClient {

    private static final String EXCHANGE_BASE_URL = "https://outlook.office365.com/adminapi/beta";

    private final TenantAccessTokenService tokenService;
    private final WebClient.Builder webClientBuilder;

    public WebClient tenantClient(String tenantId) {
        String accessToken = tokenService.getTenantExchangeAccessToken(tenantId);
        return webClientBuilder
                .baseUrl(EXCHANGE_BASE_URL + "/" + tenantId)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}