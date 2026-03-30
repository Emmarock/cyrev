package com.cyrev.iam.annotations;

import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.domain.AuthenticatedUser;
import com.cyrev.iam.entra.service.TenantSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantAdminAspect {

    private final TenantSecurityService tenantSecurityService;

    @Before("@annotation(TenantAdmin)")
    public void validateTenantAdmin() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User is not authenticated");
            throw new AccessDeniedException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            log.error("Invalid authenticated principal {}", authentication.getPrincipal());
            throw new AccessDeniedException("Invalid authenticated principal");
        }

        TenantContext tenantContext = TenantContextHolder.get();

        if (tenantContext == null) {
            throw new AccessDeniedException("Tenant context not resolved");
        }

        tenantSecurityService.validateTenantAdmin(user, tenantContext);
    }
}