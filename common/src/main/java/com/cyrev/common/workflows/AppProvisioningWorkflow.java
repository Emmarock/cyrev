package com.cyrev.common.workflows;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.ProvisioningState;
import com.cyrev.common.dtos.Role;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.Map;
import java.util.UUID;

@WorkflowInterface
public interface AppProvisioningWorkflow {

    @WorkflowMethod
    void provisionUser(UUID userId, Map<App, Role> appRoleMap);

    @SignalMethod
    void approve(String approverId);

    @SignalMethod
    void reject(String approverId, String reason);

    @QueryMethod
    ProvisioningState getState();
}
