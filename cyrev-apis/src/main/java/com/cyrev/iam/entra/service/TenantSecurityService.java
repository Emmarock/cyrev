package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.domain.AuthenticatedUser;
import com.cyrev.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class TenantSecurityService {

    private final UserService userService;
    public void validateTenantAdmin(AuthenticatedUser user, TenantContext tenantContext) {

        UUID tenantId = tenantContext.getInternalTenantId();

        boolean isAdmin = checkIfUserIsTenantAdmin(user.getUserId(), tenantId);

        if (!isAdmin) {
            log.error("User {} is not an admin of this tenant", user.getUserId());
            throw new AccessDeniedException("User is not an admin of this tenant");
        }
    }

    private boolean checkIfUserIsTenantAdmin(UUID userId, UUID tenantId) {
        log.info("Checking if user {} is an admin of this tenant {}", userId, tenantId);
        User user = userService.findTenantUser(userId, tenantId);
        return Role.ADMIN.equals(user.getRole()) || Role.SUPER_ADMIN.equals(user.getRole());
    }
}