package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.common.dtos.EntraTokenResponse;
import com.cyrev.iam.config.EntraProperties;
import com.cyrev.iam.entra.service.clients.EntraTokenClient;
import com.cyrev.iam.entra.service.clients.ResilientExchangeClient;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import com.cyrev.iam.entra.service.utils.ExchangeBootstrapStatePayload;
import com.cyrev.iam.exceptions.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Spike: programmatically bootstraps Exchange Online RBAC access for this app's
 * service principal in a customer tenant, using the consenting admin's own delegated
 * token instead of requiring them to run Exchange Online PowerShell by hand.
 *
 * Unproven — Microsoft's documented path for New-ServicePrincipal is the EXO
 * PowerShell module's own first-party client, not an arbitrary delegated
 * authorization_code token. Kept isolated from the main onboarding flow until
 * verified against a real tenant.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeBootstrapService {

    private static final String EXCHANGE_SCOPE = "https://outlook.office365.com/.default";
    // Best-known form; verify live and adjust if Exchange Online rejects it.
    private static final String ROLE_ASSIGNMENT_CMDLET = "New-ManagementRoleAssignment";
    private static final String ROLE_NAME = "Mail Recipients";

    private final EntraProperties props;
    private final ConsentStateService consentStateService;
    private final ObjectMapper objectMapper;
    private final EntraTokenClient entraTokenClient;
    private final ResilientGraphClient resilientGraphClient;
    private final ResilientExchangeClient resilientExchangeClient;

    public String buildAuthorizeUrl(String entraTenantId) {
        ExchangeBootstrapStatePayload payload = new ExchangeBootstrapStatePayload(
                LocalDateTime.now().plusMinutes(20),
                consentStateService.generate(),
                entraTenantId
        );
        try {
            String stateJson = objectMapper.writeValueAsString(payload);
            String stateEncoded = Base64.getUrlEncoder()
                    .encodeToString(stateJson.getBytes(StandardCharsets.UTF_8));
            return props.getAuthority()
                    + "/" + entraTenantId
                    + "/oauth2/v2.0/authorize"
                    + "?client_id=" + URLEncoder.encode(props.getAppId(), StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&redirect_uri=" + URLEncoder.encode(props.getBootstrapRedirectUri(), StandardCharsets.UTF_8)
                    + "&scope=" + URLEncoder.encode(EXCHANGE_SCOPE, StandardCharsets.UTF_8)
                    + "&state=" + URLEncoder.encode(stateEncoded, StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> handleCallback(String code, String state) {
        String entraTenantId = decodeAndValidateState(state);

        EntraTokenResponse delegatedToken = entraTokenClient.getTokenFromCode(code, props.getBootstrapRedirectUri());
        String accessToken = delegatedToken.getAccessToken();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tenantId", entraTenantId);
        result.put("tokenClaims", decodeNonSecretClaims(accessToken));

        String spObjectId;
        try {
            spObjectId = lookupServicePrincipalObjectId(entraTenantId);
            result.put("servicePrincipalLookup", Map.of("success", true, "objectId", spObjectId));
        } catch (Exception e) {
            String body = (e instanceof WebClientResponseException wcre) ? wcre.getResponseBodyAsString() : null;
            log.error("Service principal lookup failed for tenant {}: {}, responseBody={}", entraTenantId, e.getMessage(), body);
            Map<String, Object> lookupResult = new LinkedHashMap<>();
            lookupResult.put("success", false);
            lookupResult.put("error", String.valueOf(e.getMessage()));
            lookupResult.put("responseBody", body == null ? "" : body);
            result.put("servicePrincipalLookup", lookupResult);
            return result;
        }

        Map<String, Object> newServicePrincipalStep = runStep(() -> resilientExchangeClient.invokeDelegated(
                entraTenantId,
                accessToken,
                "New-ServicePrincipal",
                Map.of("AppId", props.getAppId(), "ObjectId", spObjectId, "DisplayName", "Cyrev")
        ), true);
        result.put("newServicePrincipal", newServicePrincipalStep);

        Map<String, Object> roleAssignmentStep = runStep(() -> resilientExchangeClient.invokeDelegated(
                entraTenantId,
                accessToken,
                ROLE_ASSIGNMENT_CMDLET,
                Map.of("Role", ROLE_NAME, "App", props.getAppId())
        ), true);
        result.put("roleAssignment", roleAssignmentStep);

        return result;
    }

    private Map<String, Object> runStep(Supplier<Map<String, Object>> action, boolean treatAlreadyExistsAsSuccess) {
        try {
            Map<String, Object> response = action.get();
            return Map.of("success", true, "response", response);
        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            if (treatAlreadyExistsAsSuccess && body != null && body.toLowerCase().contains("already exists")) {
                log.info("Bootstrap step reported already-exists, treating as success");
                return Map.of("success", true, "alreadyExisted", true, "responseBody", body);
            }
            log.error("Bootstrap step failed: error={}, responseBody={}", e.getMessage(), body);
            return Map.of("success", false, "error", String.valueOf(e.getMessage()), "responseBody", body == null ? "" : body);
        }
    }

    @SuppressWarnings("unchecked")
    private String lookupServicePrincipalObjectId(String entraTenantId) {
        Map<String, Object> response = resilientGraphClient.get(
                entraTenantId,
                "/servicePrincipals?$filter=appId eq '" + props.getAppId() + "'"
        );
        List<Map<String, Object>> value = (List<Map<String, Object>>) response.get("value");
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(
                    "No service principal found for appId " + props.getAppId() + " in tenant " + entraTenantId);
        }
        return (String) value.get(0).get("id");
    }

    /**
     * Decodes only the non-secret authorization claims (aud, scp, roles, tid, appid)
     * from the delegated token's payload, to diagnose what was actually issued without
     * ever logging or returning the token itself. JWT payloads are base64, not
     * encrypted, so the bearer already has full access to this same information.
     */
    private Map<String, Object> decodeNonSecretClaims(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return Map.of("error", "token is not a JWT");
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(padBase64Url(parts[1])), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);
            Map<String, Object> nonSecret = new LinkedHashMap<>();
            for (String claim : List.of("aud", "scp", "roles", "tid", "appid", "idtyp")) {
                if (claims.containsKey(claim)) {
                    nonSecret.put(claim, claims.get(claim));
                }
            }
            return nonSecret;
        } catch (Exception e) {
            return Map.of("error", "failed to decode token claims: " + e.getMessage());
        }
    }

    private static String padBase64Url(String segment) {
        int padding = (4 - segment.length() % 4) % 4;
        return segment + "=".repeat(padding);
    }

    private String decodeAndValidateState(String state) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            ExchangeBootstrapStatePayload payload = objectMapper.readValue(decoded, ExchangeBootstrapStatePayload.class);
            if (payload.getExpiryTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Exchange bootstrap request has expired, please retry");
            }
            consentStateService.validate(payload.getState());
            return payload.getEntraTenantId();
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid bootstrap state");
        }
    }
}
