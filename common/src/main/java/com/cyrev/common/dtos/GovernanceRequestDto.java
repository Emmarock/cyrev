package com.cyrev.common.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GovernanceRequestDto {
    private String tenantId;
    private GovernanceOperationType operationType;
    private String targetId;
    private String principalId;
    private String additionalId; // e.g., appRoleId
    private boolean approvalRequired = false;
}
