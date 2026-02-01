package com.cyrev.iam.controllers;

import com.cyrev.iam.domain.CyrevApiResponse;
import com.cyrev.iam.domain.OrganizationCreationDTO;
import com.cyrev.iam.entities.Organization;
import com.cyrev.iam.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Create a new organization
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CyrevApiResponse<Organization>> createOrganization(
            @Valid @RequestBody OrganizationCreationDTO dto) throws BadRequestException {
        Organization organization = organizationService.createOrganization(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CyrevApiResponse<>(
                        true,
                        "Organization created successfully",
                        organization
                ));
    }

    /**
     * Update an existing organization
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CyrevApiResponse<Organization>> updateOrganization(
            @PathVariable("id") UUID orgId,
            @Valid @RequestBody OrganizationCreationDTO dto) {

        Organization updated = organizationService.updateOrganization(orgId, dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Organization updated successfully",
                        updated
                ));
    }

    /**
     * Get all organizations
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CyrevApiResponse<List<Organization>>> getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Organization retrieved successfully",
                        organizations
                ));
    }

    /**
     * Get organization by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CyrevApiResponse<Organization>> getOrganizationById(@PathVariable("id") UUID orgId) {
        Organization organization = organizationService.getOrganizationById(orgId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Organization retrieved successfully",
                        organization
                ));
    }
}
