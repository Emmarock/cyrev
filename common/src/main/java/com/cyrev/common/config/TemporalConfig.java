package com.cyrev.common.config;

import io.temporal.authorization.AuthorizationTokenSupplier;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "temporal.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TemporalConfig {

    @Value("${temporal.host:temporal}")
    private String temporalHost;
    @Value("${temporal.address:temporal}")
    private String temporalAddress;
    @Value("${temporal.api-key:temporal}")
    private String temporalApiKey;

    @Value("${temporal.port:7233}")
    private int temporalPort;

    @Value("${temporal.namespace:cyrev-temporal-system}")
    private String namespace;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalAddress)
                .addApiKey(()->temporalApiKey)
                .setEnableHttps(true)
                .build();
        return WorkflowServiceStubs.newServiceStubs(options);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs, WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .build());
    }
}
