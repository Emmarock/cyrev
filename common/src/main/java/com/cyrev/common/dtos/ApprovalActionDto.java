package com.cyrev.common.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalActionDto {
    private String approver;  // admin email or ID
    private String reason;    // optional (used for rejection)
}