package com.cyrev.iam.service;

import com.cyrev.common.dtos.OrganizationCreationDTO;
import com.cyrev.common.entities.Organization;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        org.setName(dto.getName());
        return org;
    }

    public Organization toEntity(OrganizationCreationDTO dto, User user) {
        Organization org = new Organization();
        org.setName(dto.getName());

        Set<User> owners = new HashSet<>();
        owners.add(user);
        org.setOwners(owners);

        return org;
    }

    /**
     * Optional: Map Organization entity back to DTO
     */
    public OrganizationCreationDTO toDTO(Organization org) {
        OrganizationCreationDTO dto = new OrganizationCreationDTO();
        dto.setName(org.getName());
        return dto;
    }
}
