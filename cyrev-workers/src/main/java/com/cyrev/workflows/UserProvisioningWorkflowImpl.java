package com.cyrev.workflows;

import com.cyrev.common.annotations.TemporalWorkflow;
import com.cyrev.common.dtos.IdentityStatus;
import com.cyrev.common.dtos.ProvisioningState;
import com.cyrev.common.workflows.UserProvisioningWorkflow;
import com.cyrev.common.activities.UserProvisioningActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@TemporalWorkflow
public class UserProvisioningWorkflowImpl implements UserProvisioningWorkflow {

    private ProvisioningState state = ProvisioningState.PENDING;
    private boolean approved = false;
    private boolean rejected = false;
    private String rejectionReason;
    private UUID approverId;

    private final UserProvisioningActivities activities = Workflow.newActivityStub(
            UserProvisioningActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(5))
                    .setRetryOptions(
                            io.temporal.common.RetryOptions.newBuilder()
                                    .setMaximumAttempts(3)
                                    .build()
                    )
                    .build()
    );

    @Override
    public void provisionUser(UUID userId) {
        state = ProvisioningState.PENDING;

        // Step 1: Create the user
        activities.createUser(userId);

        // Step 2: Assign Employee ID
        activities.assignEmployeeId(userId);



        // Step 3: Wait for approval/rejection
        Workflow.await(() -> approved || rejected);

        if (rejected) {
            state = ProvisioningState.REJECTED;
            activities.setIdentityStatus(userId, IdentityStatus.PRE_LEAVER);
            log.info("User provisioning rejected: {}", rejectionReason);
            return;
        }

        state = ProvisioningState.PROVISIONING;

        // Step 4: Assign manager if provided
        if (approverId != null) {
            activities.assignManager(userId, approverId);
        }
        // Step 5: Set identity status to ACTIVE
        activities.setIdentityStatus(userId, IdentityStatus.ACTIVE);

        // Step 6. Activate user and

        activities.activateUser(userId);

        // Step 6: Notify user
        activities.notifyUserCreation(userId);

        state = ProvisioningState.PROVISIONED;
        log.info("User {} provisioned successfully", userId);
    }

    @Override
    public void approve(UUID approverId) {
        this.approved = true;
        this.approverId = approverId;
    }

    @Override
    public void reject(UUID approverId, String reason) {
        this.rejected = true;
        this.rejectionReason = reason;
        this.approverId = approverId;
    }

    @Override
    public ProvisioningState getState() {
        return state;
    }
}

