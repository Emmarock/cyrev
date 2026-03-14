package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.dtos.GovernanceStatus;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.common.repository.GovernanceRequestRepository;
import com.cyrev.iam.entra.service.utils.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkGovernanceProcessor {

    private final GovernanceOrchestrator orchestrator;
    private final GovernanceRequestRepository requestRepo;
    private final AuditLogService audit;
    /**
     * Execute bulk operations asynchronously for multiple principals.
     */
    @Async
    public CompletableFuture<Void> executeBulk(
            String tenantId,
            GovernanceOperationType operation,
            List<String> principalIds,
            String targetId,
            String additionalId, // e.g., appRoleId
            boolean approvalRequired
    ) {

        List<CompletableFuture<Void>> futures = principalIds.stream()
                .map(principalId -> CompletableFuture.runAsync(
                        () -> processSingle(tenantId, operation, targetId, principalId, additionalId, approvalRequired)
                ))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Process a single tenant request.
     * Delegates to the orchestrator and leverages Spring Retry in ResilientGraphClient.
     */
    private void processSingle(
            String tenantId,
            GovernanceOperationType operation,
            String targetId,
            String principalId,
            String additionalId,
            boolean approvalRequired
    ) {
        UUID requestId = null;
        try {
            // Submit the request
            requestId = orchestrator.submitRequest(
                    tenantId, operation, targetId, principalId, additionalId, approvalRequired
            );

            GovernanceRequestEntity request = requestRepo.findById(requestId).orElseThrow();

            // Execute only if approval not required or already approved
            if (request.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
                orchestrator.execute(requestId); // Spring Retry happens here via ResilientGraphClient
            }

            // Log success per item
            audit.log(
                    tenantId,
                    operation,
                    principalId,
                    GovernanceStatus.SUCCESS
            );

        } catch (Exception ex) {
            // Mark request as failed and log
            if (requestId != null) {
                GovernanceRequestEntity failedRequest = requestRepo.findById(requestId).orElse(null);
                if (failedRequest != null) {
                    failedRequest.setStatus(GovernanceStatus.FAILED);
                    failedRequest.setErrorMessage(ex.getMessage());
                    requestRepo.save(failedRequest);
                }
            }

            audit.log(
                    tenantId,
                    operation,
                    principalId,
                    GovernanceStatus.FAILED
            );

            log.error("Bulk execution failed for tenant={} principal={} operation={} error={}",
                    tenantId, principalId, operation, ex.getMessage());
        }
    }
}