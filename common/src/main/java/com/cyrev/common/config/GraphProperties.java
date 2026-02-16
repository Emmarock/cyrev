package com.cyrev.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "microsoft.graph")
@Getter
@Setter
public class GraphProperties {

    private String baseUrl;
    private String tenantId;

}
