package com.cyrev.common.dtos;

import lombok.Data;

@Data
public class AutomationJobRequest {
    private String subscriptionId;
    private String resourceGroup;
    private String automationAccountName;
}
