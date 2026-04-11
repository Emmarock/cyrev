package com.cyrev.iam.filters;

import com.cyrev.common.dtos.TenantStatus;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.domain.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantContextFilter extends OncePerRequestFilter {

    private final SaasTenantRepository saasTenantRepository;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {

                log.info("Authenticated user: {}", user);

                String tenantId = user.getTenantId();

                // ✅ CASE 1: Missing tenantId
                if (tenantId == null || tenantId.isBlank()) {

                    boolean isSuperAdmin = user.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

                    if (isSuperAdmin) {
                        log.info("Super admin {} accessing without tenant", user.getUsername());
                        chain.doFilter(request, response);
                        return;
                    }

                    log.error("Tenant ID missing for non-admin user {}", user.getUsername());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant context is required");
                    return;
                }

                // ✅ CASE 2: Validate UUID format
                UUID tenantUuid;
                try {
                    tenantUuid = UUID.fromString(tenantId);
                } catch (IllegalArgumentException ex) {
                    log.error("Invalid tenantId format: {}", tenantId);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID");
                    return;
                }

                log.info("User {} authenticated with tenantId {}", user.getUsername(), tenantId);

                // ✅ CASE 3: Fetch tenant
                SaasTenant tenant = saasTenantRepository
                        .findById(tenantUuid)
                        .orElse(null);

                if (tenant == null) {
                    log.error("Tenant {} not found", tenantId);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant not found");
                    return;
                }

                if (!tenant.isConsentGranted() && !request.getRequestURI().equals("/api/users/complete-signup")) {
                    log.error("Tenant {} has not granted consent", tenantId);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Entra consent not granted");
                    return;
                }

                if (tenant.getStatus() != TenantStatus.ACTIVE) {
                    log.error("Tenant {} is not active", tenantId);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant is not active");
                    return;
                }

                // ✅ CASE 4: Set tenant context
                TenantContextHolder.set(
                        TenantContext.builder()
                                .entraTenantId(tenant.getEntraTenantId())
                                .internalTenantId(tenant.getId())
                                .plan(tenant.getPlan())
                                .build()
                );
            }

            chain.doFilter(request, response);

        } finally {
            TenantContextHolder.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        List<String> patterns = List.of(
                "/api/auth/**",
                "/api/entra/connect-entra",
                "/api/users/invites",
                "/api/users/invites/accept"
        );

        return patterns.stream().anyMatch(p -> matcher.match(p, path));
    }
}