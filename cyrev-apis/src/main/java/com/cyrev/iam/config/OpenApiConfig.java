package com.cyrev.iam.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CYREV IAM API",
                version = "v1",
                description = "Identity, Approval and Provisioning APIs"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
        @Bean
        public GroupedOpenApi authApi() {
                return GroupedOpenApi.builder()
                        .group("auth")
                        .pathsToMatch("/api/auth/**")
                        .build();
        }

        @Bean
        public GroupedOpenApi userApi() {
                return GroupedOpenApi.builder()
                        .group("users")
                        .pathsToMatch("/api/users/**")
                        .build();
        }

}
