package com.cyrev.activities;

import com.cyrev.common.activities.AppProvisioningActivities;
import com.cyrev.common.dtos.*;
import com.cyrev.common.repository.AppAssignmentRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.AppProvisioningAdapter;
import com.cyrev.common.entities.AppAssignment;
import com.cyrev.common.entities.User;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.common.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AppProvisioningActivitiesImpl implements AppProvisioningActivities {

    private final UserRepository userRepository;
    private final AppAssignmentRepository assignmentRepository;
    private final NotificationPublisherService notificationPublisherService;
    private final Map<App, AppProvisioningAdapter> adapters;

    public AppProvisioningActivitiesImpl(UserRepository userRepository, AppAssignmentRepository assignmentRepository, NotificationPublisherService notificationPublisherService, List<AppProvisioningAdapter> adapters) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.notificationPublisherService = notificationPublisherService;
        this.adapters = adapters.stream()
                .collect(Collectors.toMap(AppProvisioningAdapter::app, a -> a));
    }

    @Override
    public void assignUserToApp(UUID userId, App app, Role role) {
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        adapters.get(app).assignUser(user, role);
        recordSuccess(user, Set.of(app));
    }


    public void recordSuccess(User user, Set<App> apps) {

        for (App app : apps) {
            AppAssignment appAssignment = assignmentRepository.findByUserIdAndApp(user.getId(), app)
                    .orElseThrow(()-> new IllegalStateException("App assignment not found"));
            appAssignment.setStatus(AssignmentStatus.ACTIVE);
            assignmentRepository.save(appAssignment);
        }
        Set<App> assignedApps = user.getAssignedApps();
        assignedApps.addAll(apps);
        user.setAssignedApps(assignedApps);
        userRepository.save(user);
        log.info("Recorded provisioning request for user={} apps={}", user.getId(), apps);
    }
    /**
     * Persist initial provisioning request for audit & idempotency
     */
    @Override
    @Transactional
    public void recordRequest(UUID userId, Map<App, Role> appRoleMap) {
        User user = userRepository.findById(userId).orElseThrow();

        appRoleMap.forEach((app, role) -> {
            assignmentRepository.findByUserIdAndApp(userId, app)
                    .orElseGet(() -> {
                        AppAssignment assignment = new AppAssignment();
                        assignment.setUser(user);
                        assignment.setApp(app);
                        assignment.setStatus(AssignmentStatus.PENDING);
                        assignment.setRole(role);
                        return assignmentRepository.save(assignment);
                    });
            log.info("Recorded provisioning request for user={} apps={}", userId, app);
        });


    }

    /**
     * Persist per-app failures (used for retry / audit)
     */
    @Override
    @Transactional
    public void recordFailures(UUID userId, Map<App, String> failures) {
        for (Map.Entry<App, String> entry : failures.entrySet()) {
            App app = entry.getKey();
            String reason = entry.getValue();

            AppAssignment assignment = assignmentRepository
                    .findByUserIdAndApp(userId, app)
                    .orElseThrow();

            assignment.setStatus(AssignmentStatus.FAILED);
            assignment.setFailureReason(reason);

            assignmentRepository.save(assignment);

            log.warn("Provisioning failed user={} app={} reason={}", userId, app, reason);
        }
    }




    /**
     * Notify approver + requester of rejection
     */
    @Override
    public void notifyRejected(UUID userId, String approverId, String reason) {
        notificationPublisherService.sendApprovalDecision(
                Notification.builder()
                        .type(NotificationType.PROVISIONING_REJECTED)
                        .userId(userId)
                        .actor(approverId)
                        .message(reason)
                        .build()
        );

        log.info("Provisioning rejected user={} by={}", userId, approverId);
    }

    /**
     * Notify completion (SUCCESS / PARTIAL / FAILED)
     */
    @Override
    public void notifyProvisioningComplete(UUID userId, ProvisioningState status) {
        notificationPublisherService.sendProvisioningComplete(userId, status);

        log.info("Provisioning completed user={} status={}", userId, status);
    }
}
