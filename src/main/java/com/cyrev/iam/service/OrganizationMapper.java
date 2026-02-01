package com.cyrev.iam.service;

import com.cyrev.iam.domain.OrganizationCreationDTO;
import com.cyrev.iam.entities.Organization;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class OrganizationMapper {

    private final UserRepository userRepository;

    public OrganizationMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Maps OrganizationCreationDTO to Organization entity.
     * Resolves ownerIds to User entities.
     */
    public Organization toEntity(OrganizationCreationDTO dto) {
        Organization org = new Organization();
        org.setCode(dto.getCode());
        org.setName(dto.getName());
        org.setContractStartDate(dto.getContractStartDate());
        org.setContractEndDate(dto.getContractEndDate());

        Set<User> owners = new HashSet<>();
        if (dto.getOwnerIds() != null) {
            dto.getOwnerIds().forEach(id -> 
                userRepository.findById(id).ifPresent(owners::add)
            );
        }
        org.setOwners(owners);

        return org;
    }

    /**
     * Optional: Map Organization entity back to DTO
     */
    public OrganizationCreationDTO toDTO(Organization org) {
        OrganizationCreationDTO dto = new OrganizationCreationDTO();
        dto.setCode(org.getCode());
        dto.setName(org.getName());
        dto.setContractStartDate(org.getContractStartDate());
        dto.setContractEndDate(org.getContractEndDate());

        Set<java.util.UUID> ownerIds = new HashSet<>();
        if (org.getOwners() != null) {
            org.getOwners().forEach(user -> ownerIds.add(user.getId()));
        }
        dto.setOwnerIds(ownerIds);

        return dto;
    }
}
