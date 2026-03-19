package com.cyrev.common.repository;

import com.cyrev.common.entities.SaasTenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SaasTenantRepository extends JpaRepository<SaasTenant, UUID> {

    Optional<SaasTenant> findByEntraTenantId(String entraTenantId);

}