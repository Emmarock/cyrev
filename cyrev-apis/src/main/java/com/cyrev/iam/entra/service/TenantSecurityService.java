package com.cyrev.iam.entra.service;

import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.domain.AuthenticatedUser;
import com.cyrev.iam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TenantSecurityService {

    private final UserService userService;
    public void validateTenantAdmin(AuthenticatedUser user, TenantContext tenantContext) {

        String tenantId = tenantContext.getEntraTenantId();

        boolean isAdmin = checkIfUserIsTenantAdmin(user.getUserId(), tenantId);

        if (!isAdmin) {
            throw new AccessDeniedException("User is not an admin of this tenant");
        }
    }

    private boolean checkIfUserIsTenantAdmin(UUID userId, String tenantId) {
        User user = userService.findTenantUser(userId, UUID.fromString(tenantId));
        return Role.ADMIN.equals(user.getRole()) || Role.SUPER_ADMIN.equals(user.getRole());
    }
}