package com.cyrev.common.entities;

import com.cyrev.common.dtos.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Where(clause = "deleted = false")
@Table(name = "business")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Business extends BaseEntity {

    @Column(name = "org_code", unique = true, nullable = false, updatable = false)
    private String orgCode;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "contract_start_date", nullable = false)
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_owner_id", nullable = false)
    private User relationshipOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private SaasTenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false)
    private ContractStatus contractStatus;

    @Column(name = "employee_id_format", nullable = false)
    private String employeeIdFormat;

    @Column(name = "employee_id_sequence", nullable = false)
    private long employeeIdSequence;
}