package com.cyrev.iam;

import com.azure.communication.email.EmailClient;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ResilientGraphClient resilientGraphClient() {
        return Mockito.mock(ResilientGraphClient.class);
    }

    @Bean
    @Primary
    public EmailClient emailClient() {
        return Mockito.mock(EmailClient.class);
    }
}