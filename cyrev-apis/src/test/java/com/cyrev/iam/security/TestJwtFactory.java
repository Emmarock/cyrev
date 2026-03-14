package com.cyrev.iam.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.List;

public class TestJwtFactory {

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tenantAdmin(String tenantId) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .claim("tid", tenantId)
                        .claim("oid", "admin-123")
                        .claim("roles", List.of("ROLE_TENANT_ADMIN"))
                )
                .authorities(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor tenantAuditor(String tenantId) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .claim("tid", tenantId)
                        .claim("oid", "auditor-123")
                        .claim("roles", List.of("ROLE_TENANT_AUDITOR"))
                )
                .authorities(new SimpleGrantedAuthority("ROLE_TENANT_AUDITOR"));
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor superAdmin() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .claim("tid", "platform")
                        .claim("oid", "platform-123")
                        .claim("roles", List.of("ROLE_SUPER_ADMIN"))
                )
                .authorities(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
    }

}