package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.common.dtos.TenantStatus;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.entra.service.EntraOrganizationService;
import com.cyrev.iam.entra.service.utils.StatePayload;
import com.cyrev.iam.exceptions.BadRequestException;
import com.cyrev.iam.service.OrganizationService;
import com.cyrev.iam.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void registerTenant(String state, UUID tenantId) {
        try{
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
            consentStateService.validate(originalState);
            // get organization
            String orgName = entraOrganizationService.verifyTenant(tenantId.toString()).getDisplayName();
            SaasTenant tenant = new SaasTenant();
            tenant.setTenantId(tenantId);
            tenant.setEntraTenantId(tenantId.toString());
            tenant.setDisplayName(orgName);
            tenant.setConsentGranted(true);
            tenant.setConsentedAt(Instant.now());
            tenant.setStatus(TenantStatus.PENDING);
            saasTenantRepository.save(tenant);
        }catch(Exception e){
            log.error("Unable to register tenant: {}", e.getMessage());
            throw new BadRequestException("Unable to register tenant");
        }
    }

    public SaasTenant findTenant( UUID tenantId) {
        return saasTenantRepository.findByEntraTenantId(tenantId.toString()).orElse(null);
    }

    public void activateTenant(UUID tenantId) {
        SaasTenant tenant = saasTenantRepository.findByEntraTenantId(tenantId.toString())
                .orElseThrow(()-> new BadRequestException("SaasTenant not found"));
        tenant.setStatus(TenantStatus.ACTIVE);
        saasTenantRepository.save(tenant);
    }
}