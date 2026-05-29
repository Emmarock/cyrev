package com.cyrev.iam.entra.service.governance;

import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Rejects on-prem-synced groups before we try to mutate their membership via Microsoft Graph,
 * since Graph cannot manage membership of directory-synced groups.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupTypeValidator {

    private static final String SELECT_FIELDS =
            "id,displayName,onPremisesSyncEnabled,mailEnabled,securityEnabled,groupTypes";

    private final ResilientGraphClient graph;

    public void requireCloudManaged(String tenantId, String groupId) {
        Map<String, Object> group = graph.get(
                tenantId,
                "/groups/" + groupId + "?$select=" + SELECT_FIELDS
        );

        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + groupId);
        }

        Object synced = group.get("onPremisesSyncEnabled");
        if (Boolean.TRUE.equals(synced)) {
            throw new IllegalStateException(
                    "Group '" + group.get("displayName")
                            + "' is synchronized from on-premises Active Directory; "
                            + "membership must be managed in AD, not via Cyrev.");
        }
    }
}