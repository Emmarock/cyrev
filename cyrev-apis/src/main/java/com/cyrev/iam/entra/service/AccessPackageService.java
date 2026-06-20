package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.AccessPackageDto;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessPackageService {

    private final ResilientGraphClient graphClient;

    @SuppressWarnings("unchecked")
    public List<AccessPackageDto> listAccessPackages() {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        log.info("Listing access packages for tenant={}", tenantId);
        Map<String, Object> response = graphClient.get(
                tenantId,
                "/identityGovernance/entitlementManagement/accessPackages?$select=id,displayName,description"
        );
        List<Map<String, Object>> value = (List<Map<String, Object>>) response.get("value");
        if (value == null) return List.of();
        log.info("Found {} access packages for tenant={}", value.size(), tenantId);
        return value.stream()
                .map(p -> AccessPackageDto.builder()
                        .id((String) p.get("id"))
                        .displayName((String) p.get("displayName"))
                        .description((String) p.get("description"))
                        .build())
                .collect(Collectors.toList());
    }
}
