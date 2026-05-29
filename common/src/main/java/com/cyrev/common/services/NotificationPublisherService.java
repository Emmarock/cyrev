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

    void publishSignupEvent(String firstname, String email);
    void publishLoginEvent( String firstname, String email);

    void publishVerificationEvent(String firstname, String email, String url);

    void publishBusinessUserPendingApproval(
            String adminEmail,
            String businessUserFullName,
            String employeeId,
            String companyName,
            UUID businessUserId
    );

    void publishBusinessUserDecision(
            String recipientEmail,
            String businessUserFullName,
            String employeeId,
            boolean approved,
            String reason
    );

    void publishAccessRequestPendingApproval(
            String adminEmail,
            String requesterFullName,
            String requesterEmail,
            String resourceCategory,
            String resourceLabel,
            UUID requestId,
            String justification
    );
}
