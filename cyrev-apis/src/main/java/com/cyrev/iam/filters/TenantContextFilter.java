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
                String tenantId = user.getTenantId();
                SaasTenant tenant = saasTenantRepository
                        .findById(UUID.fromString(tenantId))
                        .orElse(null);
                if (tenant == null || !tenant.isConsentGranted() || tenant.getStatus()!= TenantStatus.ACTIVE) {
                    log.error("Tenant with id {} was not found", tenantId);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant not registered");
                    return;
                }

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
                "/api/auth/**"
        );

        return patterns.stream().anyMatch(p -> matcher.match(p, path));
    }
}