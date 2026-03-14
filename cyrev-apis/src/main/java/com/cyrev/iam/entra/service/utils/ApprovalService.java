package com.cyrev.iam.entra.service.utils;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.dtos.GovernanceStatus;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.common.repository.GovernanceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final GovernanceRequestRepository repo;

    public void approve(UUID requestId, String approver) {

        GovernanceRequestEntity request = repo.findById(requestId)
                .orElseThrow();

        if (request.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Not pending approval");
        }

        request.setApprovalStatus(ApprovalStatus.APPROVED);
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());

        repo.save(request);
    }

    public void reject(UUID requestId, String approver) {

        GovernanceRequestEntity request = repo.findById(requestId)
                .orElseThrow();
        if (request.getApprovalStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Not pending approval");
        }
        request.setApprovalStatus(ApprovalStatus.REJECTED);
        request.setStatus(GovernanceStatus.CANCELLED);
        request.setApprovedBy(approver);
        repo.save(request);
    }
}