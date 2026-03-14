package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.dtos.GovernanceStatus;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.common.repository.GovernanceRequestRepository;
import com.cyrev.iam.entra.service.utils.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GovernanceOrchestrator {

    private final GovernanceRequestRepository governanceRequestRepository;
    private final GovernanceStrategyRegistry registry;
    private final AuditLogService audit;

    @Transactional
    public UUID submitRequest(
            String tenantId,
            GovernanceOperationType operation,
            String targetId,
            String principalId,
            String additionalId,
            boolean approvalRequired) {

        GovernanceRequestEntity request = new GovernanceRequestEntity();
        request.setTenantId(tenantId);
        request.setOperationType(operation);
        request.setTargetId(targetId);
        request.setPrincipalId(principalId);
        request.setAdditionalId(additionalId);
        request.setStatus(GovernanceStatus.PENDING);
        request.setApprovalStatus(
                approvalRequired ?
                        ApprovalStatus.PENDING_APPROVAL :
                        ApprovalStatus.NOT_REQUIRED);
        request.setCreatedAt(Instant.now());

        governanceRequestRepository.save(request);

        if (!approvalRequired) {
            execute(request.getId());
        }

        return request.getId();
    }

    @Transactional
    public void execute(UUID requestId) {

        GovernanceRequestEntity request = governanceRequestRepository.findById(requestId)
                .orElseThrow();

        if (request.getApprovalStatus() == ApprovalStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Request not approved yet");
        }

        if (request.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new IllegalStateException("Request rejected");
        }

        try {

            request.setStatus(GovernanceStatus.IN_PROGRESS);
            governanceRequestRepository.save(request);  // triggers optimistic lock check

            GovernanceStrategy strategy =
                    registry.get(request.getOperationType());

            strategy.execute(request);

            request.setStatus(GovernanceStatus.SUCCESS);
            request.setUpdatedAt(Instant.now());

        } catch (Exception ex) {

            request.setStatus(GovernanceStatus.FAILED);
            request.setErrorMessage(ex.getMessage());
        }

        governanceRequestRepository.save(request);

        audit.log(
                request.getTenantId(),
                request.getOperationType(),
                request.getId().toString(),
                request.getStatus()
        );
    }
    /**
     * NEW: Get request by ID
     */
    public Optional<GovernanceRequestEntity> getRequest(UUID requestId) {
        return governanceRequestRepository.findById(requestId);
    }
    /**
     * Fetch all requests for a given tenant and operation type
     * Useful for bulk status tracking
     */
    public List<GovernanceRequestEntity> getRequestsByTenantAndOperation(
            String tenantId,
            GovernanceOperationType operationType
    ) {
        return governanceRequestRepository.findByTenantIdAndOperationType(tenantId, operationType);
    }
    // -----------------------------------------
    // APPROVE REQUEST
    // -----------------------------------------
    @Transactional
    public void approve(UUID requestId, String tenantId, String approvedBy) {

        GovernanceRequestEntity request = validateAndRetrieveApprovalRequest(requestId, tenantId);

        request.setApprovalStatus(ApprovalStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedAt(LocalDateTime.now());
        governanceRequestRepository.save(request);

        // Automatically execute after approval
        execute(requestId);
    }

    // -----------------------------------------
    // REJECT REQUEST
    // -----------------------------------------
    @Transactional
    public void reject(UUID requestId, String adminTenantId, String rejectedBy, String reason) {

        GovernanceRequestEntity request=  validateAndRetrieveApprovalRequest(requestId, adminTenantId);
        request.setApprovalStatus(ApprovalStatus.REJECTED);
        request.setStatus(GovernanceStatus.FAILED);
        request.setApprovedBy(rejectedBy);
        request.setErrorMessage(reason);
        request.setApprovedAt(LocalDateTime.now());

        governanceRequestRepository.save(request);
    }

    private GovernanceRequestEntity validateAndRetrieveApprovalRequest(UUID requestId, String adminTenantId) {
        GovernanceRequestEntity request = governanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if(!adminTenantId.equals(request.getTenantId())) {
            throw new AccessDeniedException("Cross Tenant approval not denied");
        }

        if (!request.isApprovalRequired()) {
            throw new IllegalStateException("Approval not required");
        }

        if (request.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Request already processed");
        }
        return request;
    }

    // -----------------------------------------
    // LIST PENDING APPROVALS
    // -----------------------------------------
    public List<GovernanceRequestEntity> getPendingApprovals(String tenantId) {
        return governanceRequestRepository.findByTenantIdAndApprovalStatus(
                tenantId,
                ApprovalStatus.PENDING_APPROVAL
        );
    }
}