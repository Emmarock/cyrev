package com.cyrev.common.repository;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.entities.BusinessUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessUserRepository extends JpaRepository<BusinessUser, UUID> {

    Optional<BusinessUser> findByEmployeeId(String employeeId);

    @EntityGraph(attributePaths = {
            "business",
            "manager"
    })
    List<BusinessUser> findAllByBusiness_Id(UUID businessId);

    List<BusinessUser> findAllByBusiness_Tenant_IdAndApprovalStatus(UUID tenantId, ApprovalStatus approvalStatus);

    Optional<BusinessUser> findByIdAndBusiness_Tenant_Id(UUID id, UUID tenantId);
}