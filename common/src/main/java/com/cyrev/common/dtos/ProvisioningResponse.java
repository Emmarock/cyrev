package com.cyrev.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProvisioningResponse {
    private String workflowId;
    private String message;
}
