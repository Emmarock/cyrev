package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.ProvisioningResponse;
import com.cyrev.common.dtos.ProvisioningState;
import com.cyrev.common.dtos.UserProvisioningRequest;
import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.iam.service.UserProvisioningWorkflowStarter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/provisioning")
@RequiredArgsConstructor
@Tag(name = "User Provisioning", description = "User approval and provisioning workflows")
public class UserProvisioningController {

    private final UserProvisioningWorkflowStarter userProvisioningWorkflowStarter;

    /**
     * Start a new user provisioning workflow
     */
    @Operation(
            summary = "Start user provisioning",
            description = "Starts a new user provisioning workflow"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User provisioning workflow started"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<CyrevApiResponse<ProvisioningResponse>> provisionUser(@RequestBody UserProvisioningRequest request) {
        String workflowId = "user-" + request.getUserId();
        userProvisioningWorkflowStarter.startProvisioning(request.getUserId(), workflowId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CyrevApiResponse<>(
                        true,
                        "User provisioning workflow started",
                        ProvisioningResponse.builder().workflowId(workflowId).message("Workflow provisioning started").build()
                ));
    }

    @PostMapping("/{workflowId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CyrevApiResponse<ProvisioningResponse>> approveUser(@PathVariable String workflowId, @CurrentUserId UUID approverId) {
        userProvisioningWorkflowStarter.approveProvisioningRequest(approverId, workflowId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CyrevApiResponse<>(
                        true,
                        "Workflow Approved",
                        ProvisioningResponse.builder().workflowId(workflowId).message("Workflow Approved").build()
                ));
    }

    @PostMapping("/{workflowId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CyrevApiResponse<ProvisioningResponse>> rejectUser(@PathVariable String workflowId,
                                                                             @RequestParam String reason,
                                                                             @CurrentUserId UUID approverId) {
        userProvisioningWorkflowStarter.rejectProvisioningRequest(approverId, workflowId, reason);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CyrevApiResponse<>(
                        true,
                        "Workflow Rejected",
                        ProvisioningResponse.builder().workflowId(workflowId).message("Workflow Rejected").build()
                ));
    }


    /**
     * Query workflow state
     */
    @GetMapping("/{workflowId}/state")
    public ResponseEntity<CyrevApiResponse<ProvisioningResponse>> getState(@PathVariable String workflowId) {
        ProvisioningState state = userProvisioningWorkflowStarter.getState(workflowId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CyrevApiResponse<>(
                        true,
                        "Workflow State retrieved",
                        ProvisioningResponse.builder().workflowId(workflowId).message(state.toString()).build()
                ));
    }
}
