package com.cyrev.common.entities;

import com.cyrev.common.dtos.TenantStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saas_tenant")
@Getter
@Setter
public class SaasTenant extends TenantAwareEntity {

    @Id
    private UUID id;

    @Column(name = "entra_tenant_id", unique = true)
    private String entraTenantId;

    private String displayName;
    private boolean consentGranted;
    private Instant consentedAt;
    private String plan;

    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = Instant.now();
        status = TenantStatus.PENDING;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}