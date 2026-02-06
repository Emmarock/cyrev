package com.cyrev.common.activities;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.ProvisioningState;
import com.cyrev.common.dtos.Role;
import io.temporal.activity.ActivityInterface;

import java.util.Map;
import java.util.UUID;

@ActivityInterface
public interface AppProvisioningActivities {

    void assignUserToApp(UUID userId, App app, Role role);

    void recordFailures(UUID userId, Map<App, String> failures);

    void recordRequest(UUID userId, Map<App, Role> apps);

    void notifyRejected(UUID userId, String approverId, String reason);

    void notifyProvisioningComplete(UUID userId, ProvisioningState status);

}
