package com.cyrev.iam.config;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "azure.communication.email.enabled", havingValue = "true")
public class AzureCommunicationEmailConfig {

    @Value("${azure.communication.email.connection-string}")
    private String connectionString;

    @Bean
    public EmailClient emailClient() {
        return new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}