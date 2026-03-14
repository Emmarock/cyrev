package com.cyrev.iam.annotations;

import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.domain.AuthenticatedUser;
import com.cyrev.iam.entra.service.TenantSecurityService;
import com.cyrev.iam.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantAdminAspect {

    private final TenantSecurityService tenantSecurityService;

    @Before("@annotation(TenantAdmin)")
    public void validateTenantAdmin() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("Invalid authenticated principal");
        }

        TenantContext tenantContext = TenantContextHolder.get();

        if (tenantContext == null) {
            throw new AccessDeniedException("Tenant context not resolved");
        }

        tenantSecurityService.validateTenantAdmin(user, tenantContext);
    }
}