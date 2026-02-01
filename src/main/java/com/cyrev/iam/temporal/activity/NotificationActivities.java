package com.cyrev.iam.temporal.activity;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Notification;
import io.temporal.activity.ActivityInterface;

import java.util.Set;
import java.util.UUID;

@ActivityInterface
public interface NotificationActivities {

    void sendApprovalRequest(UUID userId, Set<App> apps);

    void sendApprovalDecision(Notification notification);

    void sendProvisioningComplete(UUID userId, String status);
}
