package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.CreateBusinessDTO;
import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.UpdateBusinessDTO;
import com.cyrev.common.entities.Business;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@Slf4j
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<Business>> registerBusiness(@Valid @RequestBody CreateBusinessDTO request) {
        TenantContext tenant = TenantContextHolder.get();
        Business business = businessService.registerBusiness(tenant.getInternalTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CyrevApiResponse<>(true, "Business registered successfully", business));
    }

    @GetMapping
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<List<Business>>> list() {
        TenantContext tenant = TenantContextHolder.get();
        List<Business> businesses = businessService.listForTenant(tenant.getInternalTenantId());
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Businesses retrieved", businesses));
    }

    @GetMapping("/{id}")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<Business>> get(@PathVariable UUID id) {
        TenantContext tenant = TenantContextHolder.get();
        Business business = businessService.getForTenant(tenant.getInternalTenantId(), id);
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Business retrieved", business));
    }

    @PutMapping("/{id}")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<Business>> update(@PathVariable UUID id, @Valid @RequestBody UpdateBusinessDTO request) {
        TenantContext tenant = TenantContextHolder.get();
        Business business = businessService.updateBusiness(tenant.getInternalTenantId(), id, request);
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Business updated", business));
    }
}