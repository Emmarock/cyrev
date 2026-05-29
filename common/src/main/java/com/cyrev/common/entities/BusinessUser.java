package com.cyrev.common.entities;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.dtos.IdentityStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Where(clause = "deleted = false")
@Table(name = "business_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BusinessUser extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "employee_id", unique = true, nullable = false, updatable = false)
    private String employeeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private BusinessUser manager;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "unit")
    private String unit;

    @Column(name = "department")
    private String department;

    @Column(name = "division")
    private String division;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_status", nullable = false)
    private IdentityStatus identityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;

    @Column(name = "approval_decided_by")
    private String approvalDecidedBy;

    @Column(name = "approval_decided_at")
    private Instant approvalDecidedAt;

    @Column(name = "approval_reason", length = 4000)
    private String approvalReason;

    @Column(name = "entra_object_id")
    private String entraObjectId;
}