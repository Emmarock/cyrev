package com.cyrev.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @PrePersist
    void assignTenant() {
        tenantId = TenantContextHolder.get().getInternalTenantId();
    }
}