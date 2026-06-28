package com.cyrev.iam.entra.service.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Exchange Online's REST admin API. The single entrypoint is
 * {@code POST /InvokeCommand}, which executes an Exchange PowerShell cmdlet by name.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResilientExchangeClient {

    private static final String INVOKE_COMMAND_URI = "/InvokeCommand";

    private final ExchangeOnlineBaseClient baseClient;

    @Retryable(
            value = { WebClientResponseException.class },
            maxAttemptsExpression = "#{${exchange.retry.max-attempts:3}}",
            backoff = @Backoff(delayExpression = "#{${exchange.retry.delay-ms:1000}}",
                    multiplier = 2)
    )
    public void invoke(String tenantId, String cmdletName, Map<String, Object> parameters) {
        Map<String, Object> payload = Map.of(
                "CmdletInput", Map.of(
                        "CmdletName", cmdletName,
                        "Parameters", parameters
                )
        );
        log.info("Invoking Exchange Online cmdlet {} for tenant {}", cmdletName, tenantId);
        baseClient.tenantClient(tenantId)
                .post()
                .uri(INVOKE_COMMAND_URI)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Retryable(
            value = { WebClientResponseException.class },
            maxAttemptsExpression = "#{${exchange.retry.max-attempts:3}}",
            backoff = @Backoff(delayExpression = "#{${exchange.retry.delay-ms:1000}}",
                    multiplier = 2)
    )
    public List<Map<String, Object>> invokeForList(String tenantId, String cmdletName, Map<String, Object> parameters) {
        Map<String, Object> payload = Map.of(
                "CmdletInput", Map.of(
                        "CmdletName", cmdletName,
                        "Parameters", parameters
                )
        );
        log.info("Invoking Exchange Online cmdlet {} (returning body) for tenant {}", cmdletName, tenantId);
        Map response = baseClient.tenantClient(tenantId)
                .post()
                .uri(INVOKE_COMMAND_URI)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (response == null || !response.containsKey("value")) {
            return List.of();
        }
        return (List<Map<String, Object>>) response.get("value");
    }

    @Recover
    public void recover(WebClientResponseException e, String tenantId, String cmdletName, Map<String, Object> parameters) {
        log.error("Exchange Online cmdlet {} failed after retries. tenantId={}, error={}, responseBody={}",
                cmdletName, tenantId, e.getMessage(), e.getResponseBodyAsString());
        throw e;
    }

    @Recover
    public List<Map<String, Object>> recoverForList(WebClientResponseException e, String tenantId, String cmdletName, Map<String, Object> parameters) {
        log.error("Exchange Online cmdlet {} (list) failed after retries. tenantId={}, error={}, responseBody={}",
                cmdletName, tenantId, e.getMessage(), e.getResponseBodyAsString());
        throw e;
    }
}