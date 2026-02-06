package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AppApprovalRequestDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private App app;

    @NotNull
    private Role role;

    private UUID approvalId;
    private String rejectionReason;
    private AssignmentStatus status = AssignmentStatus.PENDING;

}
