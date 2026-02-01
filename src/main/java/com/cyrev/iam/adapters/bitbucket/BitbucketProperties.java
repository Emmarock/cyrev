package com.cyrev.iam.adapters.bitbucket;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "adapters.bitbucket")
@Data
public class BitbucketProperties {

    private String baseUrl;
    private String username;
    private String appPassword;
    private String workspace;
}
