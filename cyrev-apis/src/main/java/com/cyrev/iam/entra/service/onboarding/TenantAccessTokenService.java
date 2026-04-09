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

    public EntraTokenResponse getLoginAccessTokenFromCode(String code) {
        return tokenClient.getTokenFromCode(code, props.getLoginRedirectUri());
    }

    public EntraTokenResponse getSignupAccessTokenFromCode(String code) {
        return tokenClient.getTokenFromCode(code, props.getSignupRedirectUri());
    }
}