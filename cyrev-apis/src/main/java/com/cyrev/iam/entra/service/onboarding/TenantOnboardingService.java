package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.common.dtos.TenantStatus;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.entra.service.clients.MicrosoftGraphClient;
import com.cyrev.iam.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantOnboardingService {

    private final SaasTenantRepository saasTenantRepository;
    private final MicrosoftGraphClient graph;
    private final ConsentStateService consentStateService;
    @Transactional
    public SaasTenant registerTenant(UUID orgId, String state, UUID tenantId) {

        SaasTenant tenant = saasTenantRepository
                .findSaasTenantByOrganization_Id(orgId)
                .orElseThrow(()-> new BadRequestException("SaasTenant not found"));
        consentStateService.validate(state);
        String orgName = graph.verifyTenant(tenantId.toString());
        tenant.setTenantId(tenantId);
        tenant.setEntraTenantId(tenantId.toString());
        tenant.setDisplayName(orgName);
        tenant.setConsentGranted(true);
        tenant.setConsentedAt(Instant.now());
        tenant.setStatus(TenantStatus.ACTIVE);

        return saasTenantRepository.save(tenant);
    }
}