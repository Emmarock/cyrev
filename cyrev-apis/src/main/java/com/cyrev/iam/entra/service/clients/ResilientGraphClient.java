package com.cyrev.iam.entra.service.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResilientGraphClient {

    private final GraphBaseClient graphBaseClient;

    /**
     * POST request to Microsoft Graph
     */
    @Retryable(
            value = { WebClientResponseException.class },
            maxAttemptsExpression = "#{${graph.retry.max-attempts:3}}",
            backoff = @Backoff(delayExpression = "#{${graph.retry.delay-ms:1000}}",
                    multiplier = 2)
    )
    public void post(String tenantId, String uri, Object body) {
        graphBaseClient.tenantClient(tenantId)
                .post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    /**
     * GET request from Microsoft Graph
     */
    @Retryable(
            value = { WebClientResponseException.class },
            maxAttemptsExpression = "#{${graph.retry.max-attempts:3}}",
            backoff = @Backoff(delayExpression = "#{${graph.retry.delay-ms:1000}}",
                    multiplier = 2)
    )
    public Map<String, Object> get(String tenantId, String uri) {
        return graphBaseClient.tenantClient(tenantId)
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
    @Retryable(
            value = { WebClientResponseException.class },
            maxAttemptsExpression = "#{${graph.retry.max-attempts:3}}",
            backoff = @Backoff(delayExpression = "#{${graph.retry.delay-ms:1000}}",
                    multiplier = 2)
    )
    public void patch(String tenantId, String uri, Object body) {
        graphBaseClient.tenantClient(tenantId)
                .patch()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    /**
     * DELETE request to Microsoft Graph
     */
    @Retryable(
            value = { WebClientResponseException.class },
            maxAttemptsExpression = "#{${graph.retry.max-attempts:3}}",
            backoff = @Backoff(delayExpression = "#{${graph.retry.delay-ms:1000}}",
                    multiplier = 2)
    )
    public void delete(String tenantId, String uri) {
        graphBaseClient.tenantClient(tenantId)
                .delete()
                .uri(uri)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    /**
     * Fallback when retries are exhausted
     */
    @Recover
    public void recover(WebClientResponseException e, String tenantId, String uri, Object body) {
        // Log the failure
        log.error("Graph call failed after retries. tenantId={}, uri={}, body={}, error={}",
                tenantId, uri, body, e.getMessage());
        throw e;
    }

    @Recover
    public Map<String, Object> recover(WebClientResponseException e, String tenantId, String uri) {
        log.error("Graph GET failed after retries. tenantId={}, uri={}, error={}",
                tenantId, uri, e.getMessage());
        throw e;
    }

    @Recover
    public void recoverDelete(WebClientResponseException e, String tenantId, String uri) {
        log.error("Graph DELETE failed after retries. tenantId={}, uri={}, error={}",
                tenantId, uri, e.getMessage());
        throw e;
    }
    public <T> T get(String tenantId, String uri, Function<Map<String, Object>, T> mapper) {
        Map<String, Object> response = get(tenantId, uri);
        return mapper.apply(response);
    }

    public Map<String, Object> getUserProfile(String accessToken, String code, String uri) {
        return graphBaseClient.authClient(accessToken,code)
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}