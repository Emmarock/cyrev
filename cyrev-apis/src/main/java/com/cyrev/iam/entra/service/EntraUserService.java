package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.EntraUser;
import com.cyrev.common.dtos.SharedMailboxDto;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.entra.mapper.EntraUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.cyrev.iam.entra.service.clients.ResilientExchangeClient;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntraUserService {

    public static final String URI = "/users";
    private final ResilientGraphClient resilientGraphClient;
    private final ResilientExchangeClient resilientExchangeClient;

    public String extractMailNickname(String email) {
        return email.split("@")[0];
    }
    public EntraUser createUser(String displayName, String mailNickname, String userPrincipalName, String tempPassword) {
        // create the user in entra
        String tenantId = getEntraTenantId();
        Map<String, Object> passwordProfile = new HashMap<>();
        passwordProfile.put("password", tempPassword);
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
                resilientGraphClient.get(tenantId, URI+"?$select=id,displayName,userPrincipalName,accountEnabled");

        List<Map<String, Object>> users =
                (List<Map<String, Object>>) response.get("value");

        return users.stream()
                .map(EntraUserMapper::fromGraph)
                .collect(Collectors.toList());
    }

    public List<SharedMailboxDto> listSharedMailboxes() {
        String tenantId = getEntraTenantId();
        log.info("Listing shared mailboxes for tenant={}", tenantId);

        List<Map<String, Object>> mailboxes = resilientExchangeClient.invokeForList(
                tenantId,
                "Get-Mailbox",
                Map.of("RecipientTypeDetails", "SharedMailbox", "ResultSize", "Unlimited")
        );

        log.info("Found {} shared mailboxes for tenant={}", mailboxes.size(), tenantId);

        return mailboxes.stream()
                .map(m -> SharedMailboxDto.builder()
                        .id((String) m.get("ExchangeObjectId"))
                        .displayName((String) m.get("DisplayName"))
                        .primarySmtpAddress((String) m.get("PrimarySmtpAddress"))
                        .userPrincipalName((String) m.get("UserPrincipalName"))
                        .build())
                .collect(Collectors.toList());
    }
}
