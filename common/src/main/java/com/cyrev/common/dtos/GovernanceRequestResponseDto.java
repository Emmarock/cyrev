package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GovernanceRequestResponseDto {

    private UUID id;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private boolean deleted;

    private String tenantId;
    private GovernanceOperationType operationType;
    private String targetId;
    private String principalId;
    private String additionalId;

    private GovernanceStatus status;
    private ApprovalStatus approvalStatus;

    private String approvedBy;
    private LocalDateTime approvedAt;
    private boolean approvalRequired;

    private String errorMessage;
}