package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.BusinessUserApprovalDTO;
import com.cyrev.common.dtos.BusinessUserDto;
import com.cyrev.common.dtos.CreateBusinessUserDTO;
import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.entities.BusinessUser;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.annotations.RelationshipManager;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.service.BusinessUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-users")
@RequiredArgsConstructor
@Slf4j
public class BusinessUserController {

    private final BusinessUserService businessUserService;

    @PostMapping
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<BusinessUserDto>> onboard(@Valid @RequestBody CreateBusinessUserDTO request) {
        TenantContext tenant = TenantContextHolder.get();
        BusinessUserDto created = businessUserService.onboardBusinessUser(
                tenant.getInternalTenantId(),
                request
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new CyrevApiResponse<>(
                        true,
                        "Business user submitted for admin approval",
                        created
                ));
    }

    @GetMapping("/pending")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<List<BusinessUserDto>>> listPending() {
        TenantContext tenant = TenantContextHolder.get();
        List<BusinessUserDto> pending = businessUserService.listPendingApprovals(tenant.getInternalTenantId());
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Pending approvals retrieved", pending));
    }

    @GetMapping("/by-business/{businessId}")
    public ResponseEntity<CyrevApiResponse<List<BusinessUserDto>>> listForBusiness(@PathVariable UUID businessId) {
        List<BusinessUserDto> users = businessUserService.listForBusiness(businessId);
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Business users retrieved", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CyrevApiResponse<BusinessUserDto>> get(@PathVariable UUID id) {
        TenantContext tenant = TenantContextHolder.get();
        BusinessUserDto businessUser = businessUserService.get(tenant.getInternalTenantId(), id);
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Business user retrieved", businessUser));
    }

    @PostMapping("/{id}/approve")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<BusinessUserDto>> approve(@PathVariable UUID id) {
        TenantContext tenant = TenantContextHolder.get();
        BusinessUserDto approved = businessUserService.approve(
                tenant.getInternalTenantId(),
                id,
                currentPrincipal(),
                tenant.getEntraTenantId()
        );
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Business user approved", approved));
    }

    @PostMapping("/{id}/reject")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<BusinessUserDto>> reject(@PathVariable UUID id, @RequestBody(required = false) BusinessUserApprovalDTO body) {
        TenantContext tenant = TenantContextHolder.get();
        BusinessUserDto rejected = businessUserService.reject(
                tenant.getInternalTenantId(),
                id,
                currentPrincipal(),
                body == null ? null : body.getReason()
        );
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Business user rejected", rejected));
    }

    @PostMapping("/{id}/offboard")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<BusinessUserDto>> offboard(@PathVariable UUID id) {
        TenantContext tenant = TenantContextHolder.get();
        BusinessUserDto offboarded = businessUserService.offboard(
                tenant.getInternalTenantId(),
                id,
                tenant.getEntraTenantId()
        );
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Business user offboarded", offboarded));
    }

    private String currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "system" : auth.getName();
    }
}