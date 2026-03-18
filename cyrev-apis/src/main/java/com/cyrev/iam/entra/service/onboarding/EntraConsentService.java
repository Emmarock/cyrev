package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.common.dtos.TenantStatus;
import com.cyrev.common.entities.Organization;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.OrganizationRepository;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.config.EntraProperties;
import com.cyrev.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntraConsentService {

    private final EntraProperties props;
    private final ConsentStateService consentStateService;
    private final UserService userService;
    private final SaasTenantRepository saasTenantRepository;
    public String buildUrl(UUID adminId) {
        User admin = userService.getUser(adminId);

        Optional<SaasTenant> tenant = saasTenantRepository.findSaasTenantByOrganization(admin.getOrganization());
        if(tenant.isEmpty()) {
            SaasTenant saasTenant = new SaasTenant();
            saasTenant.setOrganization(admin.getOrganization());
            saasTenant.setConsentGranted(false);
            saasTenant.setStatus(TenantStatus.PENDING);
            saasTenant.setTenantId(UUID.randomUUID());
            saasTenantRepository.save(saasTenant);
        }
        String state = consentStateService.generate();
        String statePayload = Base64.getUrlEncoder().encodeToString(
                ("{\"orgId\":\"" + admin.getOrganization().getId() +
                        "\",\"state\":\"" + state + "\"}")
                        .getBytes(StandardCharsets.UTF_8)
        );
        return props.getAuthority()
                + "/common/adminconsent"
                + "?client_id=" + URLEncoder.encode(props.getClientId(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(statePayload, StandardCharsets.UTF_8);
    }
}