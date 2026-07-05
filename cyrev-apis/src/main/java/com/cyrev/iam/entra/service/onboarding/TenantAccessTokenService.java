package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.common.dtos.EntraTokenResponse;
import com.cyrev.iam.config.EntraProperties;
import com.cyrev.iam.entra.service.clients.EntraTokenClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenantAccessTokenService {

    private static final String EXCHANGE_AUDIENCE = "exchange";
    private static final String EXCHANGE_SCOPE = "https://outlook.office365.com/.default";
    private static final String ARM_AUDIENCE = "arm";
    private static final String ARM_SCOPE = "https://management.azure.com/.default";

    private final EntraProperties props;
    private final TenantTokenCache cache;
    private final EntraTokenClient tokenClient;

    public String getTenantUserAccessToken(String tenantId) {

        Optional<String> cached = cache.getToken(tenantId);

        if (cached.isPresent()) {
            return cached.get();
        }

        EntraTokenResponse response = tokenClient.getToken(tenantId);

        cache.storeToken(tenantId, response.getAccessToken(), response.getExpiresIn());

        return response.getAccessToken();
    }

    public String getTenantExchangeAccessToken(String tenantId) {

        Optional<String> cached = cache.getToken(tenantId, EXCHANGE_AUDIENCE);

        if (cached.isPresent()) {
            return cached.get();
        }

        EntraTokenResponse response = tokenClient.getToken(tenantId, EXCHANGE_SCOPE);

        cache.storeToken(tenantId, EXCHANGE_AUDIENCE, response.getAccessToken(), response.getExpiresIn());

        return response.getAccessToken();
    }

    public String getTenantArmAccessToken(String tenantId) {
        Optional<String> cached = cache.getToken(tenantId, ARM_AUDIENCE);
        if (cached.isPresent()) {
            return cached.get();
        }
        EntraTokenResponse response = tokenClient.getToken(tenantId, ARM_SCOPE);
        cache.storeToken(tenantId, ARM_AUDIENCE, response.getAccessToken(), response.getExpiresIn());
        return response.getAccessToken();
    }

    public EntraTokenResponse getLoginAccessTokenFromCode(String code) {
        return tokenClient.getTokenFromCode(code, props.getLoginRedirectUri());
    }

    public EntraTokenResponse getSignupAccessTokenFromCode(String code) {
        return tokenClient.getTokenFromCode(code, props.getSignupRedirectUri());
    }
}