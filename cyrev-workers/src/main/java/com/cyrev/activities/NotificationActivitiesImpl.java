package com.cyrev.activities;

import com.cyrev.common.activities.NotificationActivities;
import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Notification;
import com.cyrev.common.dtos.ProvisioningState;
import com.cyrev.common.services.NotificationPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationActivitiesImpl implements NotificationActivities {

    private final NotificationPublisherService notificationPublisherService;

    @Override
    public void sendApprovalRequest(UUID userId, Set<App> apps) {
        notificationPublisherService.sendApprovalRequest(userId, apps);
    }

    @Override
    public void sendApprovalDecision(Notification notification) {
        notificationPublisherService.sendApprovalDecision(notification);
    }

    @Override
    public void sendProvisioningComplete(UUID userId, String status) {
        notificationPublisherService.sendProvisioningComplete(
                userId,
                ProvisioningState.valueOf(status)
        );
    }
}
