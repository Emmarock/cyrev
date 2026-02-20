package com.cyrev.iam.service;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.Address;
import com.cyrev.common.entities.Organization;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final OrganizationRepository organizationRepository;


    public User toEntity(UserCreationDTO dto) {
        User user = new User();
        user.setEmail(dto.getBusinessEmail());
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(Role.SUPER_ADMIN);
        // Company
        if (dto.getOrganization() != null) {
            organizationRepository.findByName(dto.getOrganization().getName()).ifPresent(user::setOrganization);
        }
        return user;
    }

    public Address toAddress(@NotNull AddressDto dto, Organization organization) {
        return Address.builder()
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .countryName(dto.getCountryName())
                .countryCode(dto.getCountryCode())
                .buildingNumber(dto.getBuildingNumber())
                .street(dto.getStreet())
                .organization(organization)
                .build();
    }
}
