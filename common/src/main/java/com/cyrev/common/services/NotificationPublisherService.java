package com.cyrev.common.services;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Notification;
import com.cyrev.common.dtos.ProvisioningState;

import java.util.Set;
import java.util.UUID;

public interface NotificationPublisherService {
    void sendApprovalRequest(UUID userId, Set<App> apps);

    void sendApprovalDecision(Notification notification);

    void sendProvisioningComplete(
            UUID userId,
            ProvisioningState state
    );

    void sendUserActivated(String email, String firstName, String password);

    void sendWelcomeNotification(UUID userId);

    void sendWelcomeEmail( String firstname, String email);
}
