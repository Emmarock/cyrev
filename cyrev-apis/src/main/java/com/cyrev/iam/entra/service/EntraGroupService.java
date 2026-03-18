package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.EntraGroup;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.entra.mapper.EntraGroupMapper;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import com.cyrev.iam.exceptions.BadRequestException;
import com.cyrev.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntraGroupService {

    private final ResilientGraphClient graphClient;
    private final SaasTenantRepository saasTenantRepository;
    private final UserService userService;

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


    public void deleteGroup(String tenantId, String groupId) {
        graphClient.delete(tenantId, "/groups/" + groupId);
    }

    public Map<String, Object> listGroups(String tenantId) {
        return graphClient.get(tenantId, "/groups");
    }
    private String getTenantId(UUID adminId) {
        User admin = userService.getUser(adminId);
        SaasTenant tenant= saasTenantRepository.findSaasTenantByOrganization(admin.getOrganization())
                .orElseThrow(()-> new BadRequestException("SaasTenant not found"));
        return tenant.getEntraTenantId();
    }
}