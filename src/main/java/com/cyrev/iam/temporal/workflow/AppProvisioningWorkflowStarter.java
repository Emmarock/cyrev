package com.cyrev.iam.temporal.workflow;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.ProvisioningState;
import com.cyrev.iam.domain.Role;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppProvisioningWorkflowStarter {

    private final WorkflowClient workflowClient;

    public void startProvisioning(UUID userId, Map<App, Role> appRoleMap) {
        AppProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        AppProvisioningWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue("APP_PROVISION_TASK_QUEUE")
                                .setWorkflowId("app-user-" + userId)
                                .build()
                );

        WorkflowClient.start(workflow::provisionUser, userId, appRoleMap);
    }
    public void approveProvisioningRequest(UUID approverId,UUID userId) {
        AppProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        AppProvisioningWorkflow.class,
                        "app-user-" + userId
                );

        workflow.approve(approverId.toString());
    }

    public void rejectProvisioningRequest(UUID approverId,UUID userId, String reason) {
        AppProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        AppProvisioningWorkflow.class,
                        "app-user-" + userId
                );

        workflow.reject(approverId.toString(), reason);
    }

    public ProvisioningState getState(UUID userId){
        AppProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        AppProvisioningWorkflow.class,
                        "app-user-" + userId
                );

        return workflow.getState();
    }
}
