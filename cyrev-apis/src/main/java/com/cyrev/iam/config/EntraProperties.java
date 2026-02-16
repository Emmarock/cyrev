package com.cyrev.iam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "entra")
public class EntraProperties {
    private String clientId;
    private String clientSecret;
    private String tenantId;
}
