package com.cyrev.iam.entra.controller;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.annotations.CurrentTenant;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.entra.service.governance.BulkGovernanceProcessor;
import com.cyrev.iam.entra.service.governance.GovernanceOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class GovernanceController {

    private final GovernanceOrchestrator orchestrator;
    private final BulkGovernanceProcessor bulkProcessor;

    /**
     * Submit a single governance request
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(
            @CurrentTenant String tenantId,
            @RequestBody GovernanceRequestDto dto
    ) {
        UUID requestId = orchestrator.submitRequest(
                tenantId,
                dto.getOperationType(),
                dto.getTargetId(),
                dto.getPrincipalId(),
                dto.getAdditionalId(),
                dto.isApprovalRequired()
        );

        // Optionally auto-execute if approval not required
        if (!dto.isApprovalRequired()) {
            orchestrator.execute(requestId);
        }

        Map<String, Object> response = Map.of(
                "requestId", requestId,
                "status", dto.isApprovalRequired() ? "PENDING_APPROVAL" : "EXECUTED"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Submit a bulk governance request
     */
    @PostMapping("/submit/bulk")
    @TenantAdmin
    public ResponseEntity<Map<String, Object>> submitBulk(@CurrentTenant String tenantId, @RequestBody BulkGovernanceRequestDto dto) {

        bulkProcessor.executeBulk(
                tenantId,
                dto.getOperationType(),
                dto.getPrincipalIds(),
                dto.getTargetId(),
                dto.getAdditionalId(),
                dto.isApprovalRequired()
        );

        Map<String, Object> response = Map.of(
                "tenantId", tenantId,
                "operationType", dto.getOperationType(),
                "totalRequests", dto.getPrincipalIds().size(),
                "status", dto.isApprovalRequired() ? "PENDING_APPROVAL" : "SUBMITTED"
        );

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Get request status by requestId
     */
    @GetMapping("/status/{requestId}")
    public ResponseEntity<GovernanceRequestEntity> getStatus(
            @CurrentTenant String tenantId,
            @PathVariable UUID requestId
    ) {
        return orchestrator.getRequest(requestId)
                .filter(r -> r.getTenantId().equals(tenantId)) // Ensure tenant isolation
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get bulk request status by operation type
     */
    @GetMapping("/status/bulk/{operationType}")
    public ResponseEntity<BulkRequestStatusDto> getBulkStatus(
            @CurrentTenant String tenantId,
            @PathVariable GovernanceOperationType operationType
    ) {
        List<GovernanceRequestEntity> requests =
                orchestrator.getRequestsByTenantAndOperation(tenantId, operationType);

        if (requests.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int total = requests.size();
        int completed = (int) requests.stream()
                .filter(r -> r.getStatus() == GovernanceStatus.SUCCESS)
                .count();
        int failed = (int) requests.stream()
                .filter(r -> r.getStatus() == GovernanceStatus.FAILED)
                .count();
        int pending = total - completed - failed;

        List<IndividualRequestStatusDto> individualStatuses = requests.stream()
                .map(r -> {
                    IndividualRequestStatusDto dto = new IndividualRequestStatusDto();
                    dto.setPrincipalId(r.getPrincipalId());
                    dto.setStatus(r.getStatus());
                    dto.setErrorMessage(r.getErrorMessage());
                    return dto;
                })
                .toList();

        BulkRequestStatusDto bulkStatus = new BulkRequestStatusDto();
        bulkStatus.setTenantId(tenantId);
        bulkStatus.setOperationType(operationType);
        bulkStatus.setTotal(total);
        bulkStatus.setCompleted(completed);
        bulkStatus.setFailed(failed);
        bulkStatus.setPending(pending);
        bulkStatus.setRequests(individualStatuses);

        return ResponseEntity.ok(bulkStatus);
    }
}