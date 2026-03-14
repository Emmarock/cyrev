package com.cyrev.common.repository;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GovernanceRequestRepository extends JpaRepository<GovernanceRequestEntity, UUID> {
    List<GovernanceRequestEntity> findByTenantIdAndOperationType(String tenantId, GovernanceOperationType operationType);
    List<GovernanceRequestEntity> findByTenantIdAndApprovalStatus(
            String tenantId,
            ApprovalStatus approvalStatus
    );
}
