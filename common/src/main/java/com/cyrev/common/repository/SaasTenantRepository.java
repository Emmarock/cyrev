package com.cyrev.common.repository;

import com.cyrev.common.entities.Organization;
import com.cyrev.common.entities.SaasTenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SaasTenantRepository extends JpaRepository<SaasTenant, UUID> {

    Optional<SaasTenant> findByEntraTenantId(String entraTenantId);

    Optional<SaasTenant> findSaasTenantByOrganization(Organization organization);

    Optional<SaasTenant> findSaasTenantByOrganization_Id(UUID organizationId);
    boolean existsByEntraTenantIdAndOrganization_Id(String entraTenantId, UUID organizationId);
}