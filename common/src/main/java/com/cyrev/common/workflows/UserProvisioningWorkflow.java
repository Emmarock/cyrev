package com.cyrev.common.workflows;


import com.cyrev.common.dtos.ProvisioningState;
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
