package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.AccessPackageAccessRequestDTO;
import com.cyrev.common.dtos.ApplicationAccessRequestDTO;
import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.GroupAccessRequestDTO;
import com.cyrev.common.dtos.SharedMailboxAccessRequestDTO;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.iam.annotations.RelationshipManager;
import com.cyrev.iam.service.AccessRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Employee self-service for requesting access to Entra resources (access packages,
 * applications, groups) and Exchange Online shared mailboxes. Every submission lands
 * in {@code PENDING_APPROVAL} and is decided by an admin via {@code /api/approvals}.
 */
@RestController
@RequestMapping("/api/access-requests")
@RequiredArgsConstructor
@Slf4j
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    @PostMapping("/access-packages")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<GovernanceRequestEntity>> requestAccessPackage(
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody AccessPackageAccessRequestDTO request
    ) {
        TenantContext tenant = TenantContextHolder.get();
        GovernanceRequestEntity entity = accessRequestService.requestAccessPackage(
                tenant.getInternalTenantId(),
                tenant.getEntraTenantId(),
                currentUserId,
                request
        );
        return accepted(entity);
    }

    @PostMapping("/applications")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<GovernanceRequestEntity>> requestApplication(
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody ApplicationAccessRequestDTO request
    ) {
        TenantContext tenant = TenantContextHolder.get();
        GovernanceRequestEntity entity = accessRequestService.requestApplication(
                tenant.getInternalTenantId(),
                tenant.getEntraTenantId(),
                currentUserId,
                request
        );
        return accepted(entity);
    }

    @PostMapping("/groups")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<GovernanceRequestEntity>> requestGroup(
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody GroupAccessRequestDTO request
    ) {
        TenantContext tenant = TenantContextHolder.get();
        GovernanceRequestEntity entity = accessRequestService.requestGroup(
                tenant.getInternalTenantId(),
                tenant.getEntraTenantId(),
                currentUserId,
                request
        );
        return accepted(entity);
    }

    @PostMapping("/shared-mailboxes")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<GovernanceRequestEntity>> requestSharedMailbox(
            @CurrentUserId UUID currentUserId,
            @Valid @RequestBody SharedMailboxAccessRequestDTO request
    ) {
        TenantContext tenant = TenantContextHolder.get();
        GovernanceRequestEntity entity = accessRequestService.requestSharedMailbox(
                tenant.getInternalTenantId(),
                tenant.getEntraTenantId(),
                currentUserId,
                request
        );
        return accepted(entity);
    }

    @GetMapping("/mine")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<List<GovernanceRequestEntity>>> myRequests(
            @CurrentUserId UUID currentUserId
    ) {
        TenantContext tenant = TenantContextHolder.get();
        List<GovernanceRequestEntity> requests = accessRequestService.listMyRequests(
                tenant.getEntraTenantId(),
                currentUserId,
                tenant.getInternalTenantId()
        );
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Access requests retrieved", requests));
    }

    private ResponseEntity<CyrevApiResponse<GovernanceRequestEntity>> accepted(GovernanceRequestEntity entity) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new CyrevApiResponse<>(true, "Access request submitted for approval", entity));
    }
}