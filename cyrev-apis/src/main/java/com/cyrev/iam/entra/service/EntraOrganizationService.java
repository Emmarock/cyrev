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
    public String getPrimaryDomain(String tenantId) {
        Map<String, Object> response = graphClient.get(tenantId, "/organization");
        List<Map<String, Object>> value = (List<Map<String, Object>>) response.get("value");
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("No organization found in Graph response for tenant " + tenantId);
        }
        List<Map<String, Object>> verifiedDomains = (List<Map<String, Object>>) value.get(0).get("verifiedDomains");
        if (verifiedDomains == null || verifiedDomains.isEmpty()) {
            throw new RuntimeException("No verified domains found for tenant " + tenantId);
        }
        return verifiedDomains.stream()
                .filter(d -> Boolean.TRUE.equals(d.get("isDefault")))
                .map(d -> (String) d.get("name"))
                .findFirst()
                .orElseGet(() -> (String) verifiedDomains.get(0).get("name"));
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