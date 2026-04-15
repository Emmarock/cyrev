package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.common.dtos.TenantStatus;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.entra.service.EntraOrganizationService;
import com.cyrev.iam.entra.service.utils.StatePayload;
import com.cyrev.iam.exceptions.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaasTenantService {

    private final SaasTenantRepository saasTenantRepository;
    private final ConsentStateService consentStateService;
    private final EntraOrganizationService entraOrganizationService;
    private final ObjectMapper objectMapper;
    @Transactional
    public SaasTenant registerTenant(String state, UUID tenantId, boolean isConsentGranted) {
        try{
            SaasTenant saasTenant = findTenant(tenantId);
            // This is because the tenant detail might exist before now, and we only need to grant consent
            if(saasTenant != null){
                if(!saasTenant.isConsentGranted() && isConsentGranted){
                    saasTenant.setConsentGranted(true);
                    saasTenant.setStatus(TenantStatus.ACTIVE);
                    saasTenant = saasTenantRepository.save(saasTenant);
                }
                return saasTenant;
            }

            String originalState =getOriginalState(state);
            consentStateService.validate(originalState);
            // get organization
            String orgName = entraOrganizationService.verifyTenant(tenantId.toString()).getDisplayName();
            SaasTenant tenant = new SaasTenant();
            tenant.setTenantId(tenantId);
            tenant.setEntraTenantId(tenantId.toString());
            tenant.setDisplayName(orgName);
            tenant.setConsentGranted(isConsentGranted);
            tenant.setConsentedAt(Instant.now());
            tenant.setStatus(isConsentGranted? TenantStatus.ACTIVE : TenantStatus.PENDING);
            tenant = saasTenantRepository.save(tenant);
            return tenant;
        }catch(Exception e){
            log.error("Unable to register tenant: {}", e.getMessage());
            throw new BadRequestException("Unable to register tenant");
        }
    }

    private String getOriginalState(String state) throws JsonProcessingException {
        String decoded = new String(
                Base64.getUrlDecoder().decode(state),
                StandardCharsets.UTF_8
        );
        StatePayload  statePayload = objectMapper.readValue(decoded, StatePayload.class);
        LocalDateTime expiryTime = statePayload.getExpiryTime();
        String originalState = statePayload.getState();

        if(expiryTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Consent has expired");
        }
        return originalState;
    }

    public SaasTenant findTenant( UUID tenantId) {
        return saasTenantRepository.findByEntraTenantId(tenantId.toString()).orElse(null);
    }

}