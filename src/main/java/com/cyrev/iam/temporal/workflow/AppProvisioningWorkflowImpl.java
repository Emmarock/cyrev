package com.cyrev.iam.temporal.workflow;

import com.cyrev.iam.annotations.TemporalWorkflow;
import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.ProvisioningState;
import com.cyrev.iam.domain.Role;
import com.cyrev.iam.temporal.activity.AppProvisioningActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;

@TemporalWorkflow
@Slf4j
public class AppProvisioningWorkflowImpl implements AppProvisioningWorkflow {

    private ProvisioningState state = ProvisioningState.PENDING;
    private boolean approved = false;
    private boolean rejected = false;
    private String approverId;
    private String rejectionReason;

    private final AppProvisioningActivities activities =
            Workflow.newActivityStub(
                    AppProvisioningActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofMinutes(5))
                            .setRetryOptions(RetryOptions.newBuilder()
                                    .setMaximumAttempts(3)
                                    .build())
                            .build()
            );

    @Override
    public void provisionUser(UUID userId, Map<App, Role> appRoleMap) {
        state = ProvisioningState.PENDING;

        // Record the initial request
        activities.recordRequest(userId, appRoleMap);

        // Wait for approval or rejection signal
        Workflow.await(() -> approved || rejected);

        if (rejected) {
            state = ProvisioningState.REJECTED;
            activities.notifyRejected(userId, approverId, rejectionReason);
            return;
        }

        state = ProvisioningState.PROVISIONING;

        // Provision each app asynchronously to avoid blocking workflow thread
        Map<App, Promise<Void>> appPromises = new HashMap<>();
        appRoleMap.forEach((app, role) -> {
            Promise<Void> promise = Async.procedure(() -> activities.assignUserToApp(userId, app, role));
            appPromises.put(app, promise);
        });

        // Wait for all provisioning tasks to complete
        Workflow.await(() -> appPromises.values().stream().allMatch(Promise::isCompleted));

        // Check for failures
        Map<App, String> failures = new HashMap<>();
        for (Map.Entry<App, Promise<Void>> entry : appPromises.entrySet()) {
            try {
                entry.getValue().get(); // will throw if activity failed
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                failures.put(entry.getKey(), e.getLocalizedMessage());
            }
        }

        if (failures.isEmpty()) {
            state = ProvisioningState.PROVISIONED;
        } else {
            state = ProvisioningState.PARTIALLY_PROVISIONED;
            activities.recordFailures(userId, failures);
        }

        activities.notifyProvisioningComplete(userId, state);
    }

    @Override
    public void approve(String approverId) {
        this.approved = true;
        this.approverId = approverId;
    }

    @Override
    public void reject(String approverId, String reason) {
        this.rejected = true;
        this.approverId = approverId;
        this.rejectionReason = reason;
    }

    @Override
    public ProvisioningState getState() {
        return state;
    }
}
