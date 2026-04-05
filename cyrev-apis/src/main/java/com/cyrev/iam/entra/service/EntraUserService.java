package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.EntraUser;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.entra.mapper.EntraUserMapper;
import com.cyrev.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cyrev.iam.entra.service.clients.ResilientGraphClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntraUserService {

    public static final String URI = "/users";
    private final ResilientGraphClient resilientGraphClient;
    private final SaasTenantRepository saasTenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public String extractMailNickname(String email) {
        return email.split("@")[0];
    }
    public EntraUser createUser(String displayName, String mailNickname, String userPrincipalName, String tempPassword) {
        // create the user in entra
        String tenantId = getEntraTenantId();
        Map<String, Object> passwordProfile = new HashMap<>();
        passwordProfile.put("password", passwordEncoder.encode(tempPassword));
        passwordProfile.put("forceChangePasswordNextSignIn", true);

        Map<String, Object> body = new HashMap<>();
        body.put("accountEnabled", true);
        body.put("displayName", displayName);
        body.put("mailNickname", extractMailNickname(mailNickname));
        body.put("userPrincipalName", userPrincipalName);
        body.put("passwordProfile", passwordProfile);
        log.info("Creating EntraUser: {} in tenant {} using graph url {}", body, tenantId, URI);
        resilientGraphClient.post(tenantId, URI, body);
        return EntraUserMapper.fromGraph(body);
    }


    public void updateUser(String userId, Map<String, Object> body) {
        String tenantId = getEntraTenantId();
        resilientGraphClient.patch(tenantId, "/users/" + userId, body);
    }

    private static String getEntraTenantId() {
        TenantContext tenant = TenantContextHolder.get();
        return tenant.getEntraTenantId();
    }

    public void deleteUser(String userId) {
        String tenantId = getEntraTenantId();
        resilientGraphClient.delete(tenantId, "/users/" + userId);
    }

    public EntraUser getUser(String userId) {
        String tenantId = getEntraTenantId();
        Map<String, Object> response =
                resilientGraphClient.get(tenantId, "/users/" + userId);

        return EntraUserMapper.fromGraph(response);
    }

    public List<EntraUser> listUsers() {
        String tenantId = getEntraTenantId();
        Map<String, Object> response =
                resilientGraphClient.get(tenantId, URI);

        List<Map<String, Object>> users =
                (List<Map<String, Object>>) response.get("value");

        return users.stream()
                .map(EntraUserMapper::fromGraph)
                .collect(Collectors.toList());
    }
}
