package com.cyrev.iam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "azure-automation")
public class AzureAutomationProperties {
    private String subscriptionId;
    private String resourceGroup;
    private String accountName;
    private String exchangeSetupRunbook;
}
