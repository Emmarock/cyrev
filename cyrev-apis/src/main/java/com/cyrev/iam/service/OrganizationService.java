package com.cyrev.iam.service;

import com.cyrev.common.dtos.OrganizationCreationDTO;
import com.cyrev.common.entities.Organization;
import com.cyrev.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    /**
     * Create a new Organization
     */
    @Transactional
    public Organization createOrganization(OrganizationCreationDTO dto) throws BadRequestException {
        if(organizationRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Organization with code "+dto.getCode()+" already exists ");
        }
        Organization org = organizationMapper.toEntity(dto);
        return organizationRepository.save(org);
    }

    /**
     * Update an existing Organization
     */
    @Transactional
    public Organization updateOrganization(UUID orgId, OrganizationCreationDTO dto) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + orgId));

        // Update fields
        org.setCode(dto.getCode());
        org.setName(dto.getName());
        org.setContractStartDate(dto.getContractStartDate());
        org.setContractEndDate(dto.getContractEndDate());

        // Update owners
        org.setOwners(organizationMapper.toEntity(dto).getOwners());

        return organizationRepository.save(org);
    }

    /**
     * Get all organizations
     */
    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    /**
     * Get organization by ID
     */
    @Transactional(readOnly = true)
    public Organization getOrganizationById(UUID orgId) {
        return organizationRepository.findById(orgId).orElseThrow(() -> new RuntimeException("Organization not found with id: " + orgId));
    }
}

