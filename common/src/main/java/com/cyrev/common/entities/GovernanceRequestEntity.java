package com.cyrev.common.entities;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.dtos.GovernanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "governance_requests")
@Getter @Setter
public class GovernanceRequestEntity extends BaseEntity {

    private String tenantId;

    @Enumerated(EnumType.STRING)
    private GovernanceOperationType operationType;

    private String targetId;
    private String principalId;
    private String additionalId; // appRoleId if needed

    @Enumerated(EnumType.STRING)
    private GovernanceStatus status;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    private String approvedBy;
    private LocalDateTime approvedAt;
    private boolean isApprovalRequired;

    @Column(length = 4000)
    private String errorMessage;
}