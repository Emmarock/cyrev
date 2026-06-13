package com.cyrev.iam.scim.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ScimAuthFilterConfig {

    @Bean
    public FilterRegistrationBean<ScimAuthFilter> scimAuthFilterRegistration(
            @Value("${scim.token}") String scimToken
    ) {
        FilterRegistrationBean<ScimAuthFilter> registration =
                new FilterRegistrationBean<>(new ScimAuthFilter(scimToken));
        registration.addUrlPatterns("/scim/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}