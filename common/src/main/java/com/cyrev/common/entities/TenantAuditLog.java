package com.cyrev.common.entities;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.dtos.GovernanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_audit_logs")
@Getter
@Setter
public class TenantAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String tenantId;

    @Enumerated(EnumType.STRING)
    private GovernanceOperationType action;

    private String actor;
    private String referenceId;

    @Enumerated(EnumType.STRING)
    private GovernanceStatus status;

    @Column(length = 4000)
    private String details;

    private LocalDateTime timestamp;
}