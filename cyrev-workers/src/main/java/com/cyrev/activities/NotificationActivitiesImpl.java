package com.cyrev.activities;

import com.cyrev.common.activities.NotificationActivities;
import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Notification;
import com.cyrev.common.dtos.ProvisioningState;
import com.cyrev.common.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationActivitiesImpl implements NotificationActivities {

    private final NotificationService emailService;

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
