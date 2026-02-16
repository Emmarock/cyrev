package com.cyrev.common.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphMailService {

    private final WebClient graphWebClient;
    private final AzureTokenService tokenService;
    private final GraphUrlBuilder  graphUrlBuilder;
    @Value("${mail.sender:info@ayokunmidapo.com}")
    private String sender;

    public void sendMail(String to, String subject, String body) {

        String token = tokenService.getAccessToken();
        graphWebClient.post()
                .uri(graphUrlBuilder.sendMailUrl(sender))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(buildRequest(to, subject, body))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private Map<String, Object> buildRequest(String to, String subject, String body) {
        return Map.of(
                "message", Map.of(
                        "subject", subject,
                        "body", Map.of(
                                "contentType", "Text",
                                "content", body
                        ),
                        "toRecipients", List.of(
                                Map.of("emailAddress",
                                        Map.of("address", to))
                        )
                )
        );
    }
}
