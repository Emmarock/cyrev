package com.cyrev.common.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkGovernanceRequestDto {
    private String tenantId;
    private GovernanceOperationType operationType;
    private String targetId;
    private List<String> principalIds;
    private String additionalId; // optional
    private boolean approvalRequired = false;
}