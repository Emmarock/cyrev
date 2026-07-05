package com.cyrev.iam.entra.controller;

import com.cyrev.common.dtos.AutomationJobStatus;
import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.DistributionGroupRequest;
import com.cyrev.common.dtos.SharedMailboxRequest;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.annotations.RelationshipManager;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.entra.service.onboarding.AzureAutomationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entra/exchange")
@RequiredArgsConstructor
@Tag(name = "Exchange Management", description = "Manage shared mailboxes and distribution groups via Azure Automation")
public class ExchangeManagementController {

    private final AzureAutomationService automationService;

    @PostMapping("/shared-mailbox/add")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<String>> addUserToSharedMailbox(
            @RequestBody SharedMailboxRequest request) {
        String tenantId = TenantContextHolder.get().getEntraTenantId();
        String jobId = automationService.addUserToSharedMailbox(tenantId, request);
        return ResponseEntity.ok(new CyrevApiResponse<>(true,
                "Add user to shared mailbox submitted. Job ID: " + jobId, jobId));
    }

    @PostMapping("/shared-mailbox/remove")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<String>> removeUserFromSharedMailbox(
            @RequestBody SharedMailboxRequest request) {
        String tenantId = TenantContextHolder.get().getEntraTenantId();
        String jobId = automationService.removeUserFromSharedMailbox(tenantId, request);
        return ResponseEntity.ok(new CyrevApiResponse<>(true,
                "Remove user from shared mailbox submitted. Job ID: " + jobId, jobId));
    }

    @PostMapping("/distribution-group/add")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<String>> addUserToDistributionGroup(
            @RequestBody DistributionGroupRequest request) {
        String tenantId = TenantContextHolder.get().getEntraTenantId();
        String jobId = automationService.addUserToDistributionGroup(tenantId, request);
        return ResponseEntity.ok(new CyrevApiResponse<>(true,
                "Add user to distribution group submitted. Job ID: " + jobId, jobId));
    }

    @PostMapping("/distribution-group/remove")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<String>> removeUserFromDistributionGroup(
            @RequestBody DistributionGroupRequest request) {
        String tenantId = TenantContextHolder.get().getEntraTenantId();
        String jobId = automationService.removeUserFromDistributionGroup(tenantId, request);
        return ResponseEntity.ok(new CyrevApiResponse<>(true,
                "Remove user from distribution group submitted. Job ID: " + jobId, jobId));
    }

    @GetMapping("/jobs/{jobId}")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<AutomationJobStatus>> getJobStatus(
            @PathVariable String jobId) {
        AutomationJobStatus status = automationService.getJobStatus(jobId);
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Job status retrieved", status));
    }
}
