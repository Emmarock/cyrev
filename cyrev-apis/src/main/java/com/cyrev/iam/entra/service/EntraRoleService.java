package com.cyrev.iam.entra.service;

import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EntraRoleService {

    private final ResilientGraphClient graphClient;

    public void assignRoleToUser(
            String userId,
            String principalId,
            String appRoleId,
            String resourceId
    ) {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        Map<String, Object> body = new HashMap<>();
        body.put("principalId", principalId);
        body.put("resourceId", resourceId);
        body.put("appRoleId", appRoleId);

        graphClient.post(
                tenantId,
                "/v1.0/users/" + userId + "/appRoleAssignments",
                body
        );
    }
}
