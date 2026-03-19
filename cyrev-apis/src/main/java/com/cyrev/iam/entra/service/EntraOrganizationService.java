package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.EntraOrganization;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntraOrganizationService {

    private final ResilientGraphClient graphClient;


    public EntraOrganization getOrganization() {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        return graphClient.get(
                tenantId,
                "/organization",
                this::mapToOrganization
        );
    }
    public EntraOrganization verifyTenant(String tenantId) {
        return graphClient.get(
                tenantId,
                "/organization",
                this::mapToOrganization
        );
    }
    @SuppressWarnings("unchecked")
    private EntraOrganization mapToOrganization(Map<String, Object> response) {

        List<Map<String, Object>> value =
                (List<Map<String, Object>>) response.get("value");

        if (value == null || value.isEmpty()) {
            throw new RuntimeException("No organization found in Graph response");
        }

        Map<String, Object> org = value.get(0);

        List<String> domains = Optional.ofNullable(
                        (List<Map<String, Object>>) org.get("verifiedDomains")
                ).orElse(List.of())
                .stream()
                .map(d -> (String) d.get("name"))
                .toList();

        return EntraOrganization.builder()
                .id((String) org.get("id"))
                .displayName((String) org.get("displayName"))
                .tenantType((String) org.get("tenantType"))
                .verifiedDomains(domains)
                .build();
    }
}