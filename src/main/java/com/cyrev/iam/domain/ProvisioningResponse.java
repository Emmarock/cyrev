package com.cyrev.iam.domain;

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
