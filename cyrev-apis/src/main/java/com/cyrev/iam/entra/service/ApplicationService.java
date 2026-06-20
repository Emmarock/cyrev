package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.AppRoleDto;
import com.cyrev.common.dtos.ServicePrincipalDto;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final GraphServiceClient<?> graphClient;
    private final ResilientGraphClient resilientGraphClient;

    public Application createApplication(String displayName) {
        Application app = new Application();
        app.displayName = displayName;
        return graphClient.applications()
                .buildRequest()
                .post(app);
    }

    public void deleteApplication(String appId) {
        graphClient.applications(appId)
                .buildRequest()
                .delete();
    }

    @SuppressWarnings("unchecked")
    public List<ServicePrincipalDto> listServicePrincipals() {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        Map<String, Object> response = resilientGraphClient.get(
                tenantId,
                "/servicePrincipals?$select=id,displayName,appRoles"
        );
        List<Map<String, Object>> value = (List<Map<String, Object>>) response.get("value");
        if (value == null) return List.of();
        return value.stream()
                .map(sp -> ServicePrincipalDto.builder()
                        .id((String) sp.get("id"))
                        .displayName((String) sp.get("displayName"))
                        .appRoles(mapAppRoles((List<Map<String, Object>>) sp.get("appRoles")))
                        .build())
                .collect(Collectors.toList());
    }

    private List<AppRoleDto> mapAppRoles(List<Map<String, Object>> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .filter(r -> Boolean.TRUE.equals(r.get("isEnabled")))
                .map(r -> AppRoleDto.builder()
                        .id((String) r.get("id"))
                        .displayName((String) r.get("displayName"))
                        .description((String) r.get("description"))
                        .build())
                .collect(Collectors.toList());
    }
}
