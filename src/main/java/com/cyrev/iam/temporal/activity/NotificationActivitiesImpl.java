package com.cyrev.iam.temporal.activity;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Notification;
import com.cyrev.iam.domain.ProvisioningState;
import com.cyrev.iam.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationActivitiesImpl implements NotificationActivities {

    private final EmailNotificationService emailService;

    @Override
    public void sendApprovalRequest(UUID userId, Set<App> apps) {
        emailService.sendApprovalRequest(userId, apps);
    }

    @Override
    public void sendApprovalDecision(Notification notification) {
        emailService.sendApprovalDecision(notification);
    }

    @Override
    public void sendProvisioningComplete(UUID userId, String status) {
        emailService.sendProvisioningComplete(
                userId,
                ProvisioningState.valueOf(status)
        );
    }
}
