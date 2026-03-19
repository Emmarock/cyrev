package com.cyrev.iam.service;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.Address;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public static String emailToUsername(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return email.replace("@", "_")
                .replace(".", "_");
    }

    public User toEntity(UserCreationDTO dto) {
        String username= emailToUsername(dto.getBusinessEmail());
        User user = new User();
        user.setEmail(dto.getBusinessEmail());
        user.setUsername(username);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(Role.SUPER_ADMIN);
        user.setStatus(UserStatus.PENDING);
        user.setAuthProvider(dto.getAuthProvider()==null?AuthProvider.CYREV:dto.getAuthProvider());
        return user;
    }

    public Address toAddress(@NotNull AddressDto dto) {
        return Address.builder()
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .countryName(dto.getCountryName())
                .countryCode(dto.getCountryCode())
                .buildingNumber(dto.getBuildingNumber())
                .street(dto.getStreet())
                .build();
    }
}
