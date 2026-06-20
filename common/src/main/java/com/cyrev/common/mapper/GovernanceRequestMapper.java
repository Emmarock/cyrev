package com.cyrev.common.mapper;

import com.cyrev.common.dtos.GovernanceRequestResponseDto;
import com.cyrev.common.entities.GovernanceRequestEntity;
import org.springframework.stereotype.Component;

@Component
public final class GovernanceRequestMapper {

    public GovernanceRequestResponseDto toDto(GovernanceRequestEntity entity) {

        return GovernanceRequestResponseDto.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .deleted(entity.isDeleted())

                .tenantId(entity.getTenantId())
                .operationType(entity.getOperationType())
                .targetId(entity.getTargetId())
                .principalId(entity.getPrincipalId())
                .additionalId(entity.getAdditionalId())

                .status(entity.getStatus())
                .approvalStatus(entity.getApprovalStatus())

                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .approvalRequired(entity.isApprovalRequired())

                .errorMessage(entity.getErrorMessage())
                .build();
    }
}