package com.cyrev.common.repository;

import com.cyrev.common.entities.Business;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findByOrgCode(String orgCode);

    boolean existsByOrgCode(String orgCode);

    List<Business> findAllByTenant_Id(UUID tenantId);

    Optional<Business> findByIdAndTenant_Id(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Business> findWithLockingById(UUID id);
}