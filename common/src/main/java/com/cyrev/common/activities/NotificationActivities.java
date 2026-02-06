package com.cyrev.common.activities;


import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Notification;
import io.temporal.activity.ActivityInterface;

import java.util.Set;
import java.util.UUID;

@ActivityInterface
public interface NotificationActivities {

    void sendApprovalRequest(UUID userId, Set<App> apps);

    void sendApprovalDecision(Notification notification);

    void sendProvisioningComplete(UUID userId, String status);
}
