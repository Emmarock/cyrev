package com.cyrev.iam.temporal.workflow;

import com.cyrev.iam.domain.IdentityStatus;
import com.cyrev.iam.domain.ProvisioningState;
import com.cyrev.iam.domain.UserCreationDTO;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.SignalMethod;

import java.util.UUID;

@WorkflowInterface
public interface UserProvisioningWorkflow {

    @WorkflowMethod
    void provisionUser(UUID userId);

    @SignalMethod
    void approve(UUID approverId);

    @SignalMethod
    void reject(UUID approverId, String reason);

    @QueryMethod
    ProvisioningState getState();
}
