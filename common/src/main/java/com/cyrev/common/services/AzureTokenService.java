package com.cyrev.common.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AzureTokenService {
    private final WebClient azureAuthWebClient;

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    private final WebClient webClient = WebClient.create();

    public String getAccessToken() {
        String tokenUrl = "https://login.microsoftonline.com/" +
                tenantId + "/oauth2/v2.0/token";

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&scope=https://graph.microsoft.com/.default" +
                        "&grant_type=client_credentials"
                )
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText())
                .block();
    }
}
