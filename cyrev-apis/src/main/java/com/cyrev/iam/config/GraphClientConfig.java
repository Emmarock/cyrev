package com.cyrev.iam.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GraphClientConfig {

    @Bean
    public GraphServiceClient<?> graphClient(EntraProperties props) {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .tenantId(props.getTenantId())
                .build();

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                List.of("https://graph.microsoft.com/.default"),
                credential
        );

        return GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }
}
