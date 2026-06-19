package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BusinessUserDto {

    private UUID id;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private boolean deleted;

    private String firstName;
    private String lastName;
    private String employeeId;

    private UUID businessId;

    private UUID managerId;
    private String managerName;

    private LocalDate startDate;
    private LocalDate endDate;

    private String unit;
    private String department;
    private String division;

    private IdentityStatus identityStatus;
    private ApprovalStatus approvalStatus;

    private String approvalDecidedBy;
    private Instant approvalDecidedAt;
    private String approvalReason;

    private String entraObjectId;
}