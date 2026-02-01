package com.cyrev.iam.service;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Role;
import com.cyrev.iam.domain.UserCreationDTO;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.OrganizationRepository;
import com.cyrev.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public User toEntity(UserCreationDTO dto) {
        User user = new User();

        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setStartDate(dto.getStartDate());
        user.setEndDate(dto.getEndDate());
        user.setDepartment(dto.getDepartment());
        user.setUnit(dto.getUnit());
        user.setDivision(dto.getDivision());
        user.setIdentityStatus(dto.getIdentityStatus());
        user.setRole(Role.valueOf(dto.getRole()));

        // Assigned Apps
        if (dto.getAssignedApps() != null) {
            Set<App> apps = dto.getAssignedApps().stream()
                    .map(App::valueOf)
                    .collect(Collectors.toSet());
            user.setAssignedApps(apps);
        }

        // Manager
        if (dto.getManagerId() != null) {
            userRepository.findById(dto.getManagerId()).ifPresent(user::setManager);
        }

        // Company
        if (dto.getOrganizationCode() != null) {
            organizationRepository.findByCode(dto.getOrganizationCode()).ifPresent(user::setOrganization);
        }

        return user;
    }
}
