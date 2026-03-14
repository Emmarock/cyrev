package com.cyrev.iam.entra.controller;

import com.cyrev.common.dtos.ApprovalActionDto;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.annotations.CurrentTenant;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.entra.service.governance.GovernanceOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final GovernanceOrchestrator orchestrator;

    /**
     * Get all pending approvals for a tenant
     */
    @GetMapping("/pending/{tenantId}")
    @TenantAdmin
    public ResponseEntity<List<GovernanceRequestEntity>> getPending(
            @PathVariable String tenantId
    ) {
        List<GovernanceRequestEntity> requests =
                orchestrator.getPendingApprovals(tenantId);

        return ResponseEntity.ok(requests);
    }

    /**
     * Approve a governance request
     */
    @PostMapping("/{requestId}/approve")
    @TenantAdmin
    public ResponseEntity<String> approve(@PathVariable UUID requestId, @CurrentTenant String tenantId, JwtAuthenticationToken auth) {

        orchestrator.approve(requestId, tenantId, auth.getName());

        return ResponseEntity.ok("Approved and executed");
    }
    /**
     * Reject a governance request
     */
    @PostMapping("/{requestId}/reject")
    @TenantAdmin
    public ResponseEntity<String> reject(
            @PathVariable UUID requestId,
            @CurrentTenant String tenantId,
            @RequestBody ApprovalActionDto dto
    ) {
        orchestrator.reject(requestId, tenantId, dto.getApprover(), dto.getReason());
        return ResponseEntity.ok("Request rejected");
    }

    /**
     * Get specific approval request details
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<GovernanceRequestEntity> getDetails(
            @PathVariable UUID requestId
    ) {
        return orchestrator.getRequest(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}