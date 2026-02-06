package com.cyrev.iam.service;

import com.cyrev.common.dtos.ProvisioningState;
import com.cyrev.common.workflows.UserProvisioningWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProvisioningWorkflowStarter {
    private final WorkflowClient workflowClient;
    public void startProvisioning(UUID userId, String workflowId) {
        UserProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        UserProvisioningWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue("USER_PROVISION_TASK_QUEUE")
                                .setWorkflowId(workflowId)
                                .build()
                );

        WorkflowClient.start(workflow::provisionUser, userId);
    }
    public void approveProvisioningRequest(UUID approverId, String workflowId) {
        UserProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        UserProvisioningWorkflow.class,
                        workflowId
                );

        workflow.approve(approverId);
    }

    public void rejectProvisioningRequest(UUID approverId,String workflowId, String reason) {
        UserProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        UserProvisioningWorkflow.class,
                        workflowId
                );

        workflow.reject(approverId, reason);
    }

    public ProvisioningState getState(String workflowId){
        UserProvisioningWorkflow workflow =
                workflowClient.newWorkflowStub(
                        UserProvisioningWorkflow.class,
                        workflowId
                );

        return workflow.getState();
    }
}
