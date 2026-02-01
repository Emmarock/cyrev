package com.cyrev.iam.service;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Notification;
import com.cyrev.iam.domain.ProvisioningState;

import java.util.Set;
import java.util.UUID;

public interface NotificationService {
    void sendApprovalRequest(UUID userId, Set<App> apps);

    void sendApprovalDecision(Notification notification);

    void sendProvisioningComplete(
            UUID userId,
            ProvisioningState state
    );

    void sendUserActivated(String email, String firstName, String password);

    void sendWelcomeNotification(UUID userId);
}
