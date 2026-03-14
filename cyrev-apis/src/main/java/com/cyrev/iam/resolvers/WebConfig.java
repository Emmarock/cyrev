package com.cyrev.iam.resolvers;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentTenantArgumentResolver currentTenantResolver;

    public WebConfig(CurrentTenantArgumentResolver currentTenantResolver) {
        this.currentTenantResolver = currentTenantResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentTenantResolver);
    }
}