package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.EntraGroup;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.entra.mapper.EntraGroupMapper;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EntraGroupService {

    private final ResilientGraphClient graphClient;

    public EntraGroup createGroup( EntraGroup entraGroup) {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        Map<String, Object> body = new HashMap<>();
        body.put("displayName", entraGroup.getDisplayName());
        body.put("mailEnabled", false);
        body.put("securityEnabled", true);
        body.put("mailNickname", entraGroup.getMailNickname());

        graphClient.post(tenantId, "/v1.0/groups", body);
        return EntraGroupMapper.fromGraph(body);
    }
}