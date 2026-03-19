package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.EntraUser;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.entra.mapper.EntraUserMapper;
import com.cyrev.iam.exceptions.BadRequestException;
import com.cyrev.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cyrev.iam.entra.service.clients.ResilientGraphClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntraUserService {

    public static final String URI = "/users";
    private final ResilientGraphClient resilientGraphClient;
    private final SaasTenantRepository saasTenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public EntraUser createUser(String displayName, String mailNickname, String userPrincipalName, String tempPassword) {
        // create the user in entra
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        Map<String, Object> passwordProfile = new HashMap<>();
        passwordProfile.put("password", passwordEncoder.encode(tempPassword));
        passwordProfile.put("forceChangePasswordNextSignIn", true);

        Map<String, Object> body = new HashMap<>();
        body.put("accountEnabled", true);
        body.put("displayName", displayName);
        body.put("mailNickname", mailNickname);
        body.put("userPrincipalName", userPrincipalName);
        body.put("passwordProfile", passwordProfile);

        resilientGraphClient.post(tenantId, URI, body);

        return EntraUserMapper.fromGraph(body);
    }


    public void updateUser(String userId, Map<String, Object> body) {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        resilientGraphClient.patch(tenantId, "/users/" + userId, body);
    }

    public void deleteUser(String userId) {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        resilientGraphClient.delete(tenantId, "/users/" + userId);
    }

    public EntraUser getUser(String userId) {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        Map<String, Object> response =
                resilientGraphClient.get(tenantId, "/users/" + userId);

        return EntraUserMapper.fromGraph(response);
    }

    public List<EntraUser> listUsers() {
        TenantContext tenant = TenantContextHolder.get();
        String tenantId = tenant.getEntraTenantId();
        Map<String, Object> response =
                resilientGraphClient.get(tenantId, URI);

        List<Map<String, Object>> users =
                (List<Map<String, Object>>) response.get("value");

        return users.stream()
                .map(EntraUserMapper::fromGraph)
                .collect(Collectors.toList());
    }
}
