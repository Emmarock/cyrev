package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BusinessResponseDTO {

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;

    private String orgCode;
    private String companyName;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private ContractStatus contractStatus;
    private String employeeIdFormat;
    private long employeeIdSequence;

    private UUID relationshipOwnerId;
    private String relationshipOwnerName;

    private UUID tenantId;
}
