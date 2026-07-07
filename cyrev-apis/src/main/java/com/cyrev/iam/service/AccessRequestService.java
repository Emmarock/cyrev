package com.cyrev.iam.service;

import com.cyrev.common.dtos.AccessPackageAccessRequestDTO;
import com.cyrev.common.dtos.ApplicationAccessRequestDTO;
import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.dtos.GroupAccessRequestDTO;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.dtos.SharedMailboxAccessRequestDTO;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.GovernanceRequestRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.iam.entra.service.governance.GovernanceOrchestrator;
import com.cyrev.iam.exceptions.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Employee-facing self-service for access requests. Every request is submitted on
 * behalf of the caller, always lands in {@code PENDING_APPROVAL}, and notifies the
 * tenant admins.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccessRequestService {

    private static final List<Role> APPROVER_ROLES = List.of(Role.ADMIN, Role.SUPER_ADMIN);
    private static final String CATEGORY_ACCESS_PACKAGE = "Access package";
    private static final String CATEGORY_APPLICATION = "Application";
    private static final String CATEGORY_GROUP = "Group";
    private static final String CATEGORY_SHARED_MAILBOX = "Shared mailbox";

    private final GovernanceOrchestrator orchestrator;
    private final GovernanceRequestRepository governanceRequestRepository;
    private final UserRepository userRepository;
    private final NotificationPublisherService notificationPublisher;

    @Transactional
    public GovernanceRequestEntity requestAccessPackage(
            UUID tenantInternalId,
            String entraTenantId,
            UUID requesterUserId,
            AccessPackageAccessRequestDTO dto
    ) {
        User requester = requireRequester(requesterUserId, tenantInternalId);
        UUID requestId = orchestrator.submitRequest(
                entraTenantId,
                GovernanceOperationType.ACCESS_PACKAGE_ASSIGN,
                dto.getAccessPackageId(),
                requester.getProviderUserId(),
                null,
                true
        );
        notifyApprovers(tenantInternalId, requester, CATEGORY_ACCESS_PACKAGE,
                dto.getAccessPackageId(), requestId, dto.getJustification());
        return retrieve(requestId);
    }

    @Transactional
    public GovernanceRequestEntity requestApplication(
            UUID tenantInternalId,
            String entraTenantId,
            UUID requesterUserId,
            ApplicationAccessRequestDTO dto
    ) {
        User requester = requireRequester(requesterUserId, tenantInternalId);
        UUID requestId = orchestrator.submitRequest(
                entraTenantId,
                GovernanceOperationType.APP_ROLE_ASSIGN,
                dto.getServicePrincipalId(),
                requester.getProviderUserId(),
                dto.getAppRoleId(),
                true
        );
        String label = dto.getServicePrincipalId() + " (role " + dto.getAppRoleId() + ")";
        notifyApprovers(tenantInternalId, requester, CATEGORY_APPLICATION,
                label, requestId, dto.getJustification());
        return retrieve(requestId);
    }

    @Transactional
    public GovernanceRequestEntity requestGroup(
            UUID tenantInternalId,
            String entraTenantId,
            UUID requesterUserId,
            GroupAccessRequestDTO dto
    ) {
        User requester = requireRequester(requesterUserId, tenantInternalId);
        UUID requestId = orchestrator.submitRequest(
                entraTenantId,
                GovernanceOperationType.GROUP_ADD,
                dto.getGroupId(),
                requester.getProviderUserId(),
                null,
                true
        );
        notifyApprovers(tenantInternalId, requester, CATEGORY_GROUP,
                dto.getGroupId(), requestId, dto.getJustification());
        return retrieve(requestId);
    }

    @Transactional
    public GovernanceRequestEntity requestSharedMailbox(
            UUID tenantInternalId,
            String entraTenantId,
            UUID requesterUserId,
            SharedMailboxAccessRequestDTO dto
    ) {
        User requester = requireRequester(requesterUserId, tenantInternalId);
        String accessRight = dto.getAccessRight() == null || dto.getAccessRight().isBlank()
                ? "FullAccess"
                : dto.getAccessRight();
        UUID requestId = orchestrator.submitRequest(
                entraTenantId,
                GovernanceOperationType.SHARED_MAILBOX_ACCESS_GRANT,
                dto.getSharedMailboxId(),
                emailOrFallback(requester),
                accessRight,
                true
        );
        String label = dto.getSharedMailboxId() + " (" + accessRight + ")";
        notifyApprovers(tenantInternalId, requester, CATEGORY_SHARED_MAILBOX,
                label, requestId, dto.getJustification());
        return retrieve(requestId);
    }

    public List<GovernanceRequestEntity> listMyRequests(String entraTenantId, UUID requesterUserId, UUID tenantInternalId) {
        User requester = requireRequester(requesterUserId, tenantInternalId);
        return governanceRequestRepository.findByTenantIdAndPrincipalIdOrderByCreatedAtDesc(
                entraTenantId,
                requester.getProviderUserId() != null ? requester.getProviderUserId() : emailOrFallback(requester)
        );
    }

    private User requireRequester(UUID userId, UUID tenantInternalId) {
        User user = userRepository.findUserByIdAndTenant_Id(userId, tenantInternalId)
                .orElseThrow(() -> new EntityNotFoundException("User not found in tenant: " + userId));
        if (user.getProviderUserId() == null || user.getProviderUserId().isBlank()) {
            log.error( "Current user {} has no Entra identity linked; cannot submit access request", userId);
            throw new BadRequestException(
                    "Current user has no Entra identity linked; cannot submit access request");
        }
        return user;
    }

    private GovernanceRequestEntity retrieve(UUID requestId) {
        return governanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not persisted: " + requestId));
    }

    private void notifyApprovers(
            UUID tenantInternalId,
            User requester,
            String category,
            String resourceLabel,
            UUID requestId,
            String justification
    ) {
        List<User> admins = userRepository.findAllByTenant_IdAndRoleIn(tenantInternalId, APPROVER_ROLES);
        if (admins.isEmpty()) {
            log.warn("No admins to notify for tenant {} access request {}", tenantInternalId, requestId);
            return;
        }
        String requesterName = requester.getFirstName() + " " + requester.getLastName();
        admins.forEach(admin -> notificationPublisher.publishAccessRequestPendingApproval(
                admin.getEmail(),
                requesterName,
                requester.getEmail(),
                category,
                resourceLabel,
                requestId,
                justification
        ));
    }

    private String emailOrFallback(User user) {
        return user.getEmail() != null ? user.getEmail() : user.getProviderUserId();
    }
}