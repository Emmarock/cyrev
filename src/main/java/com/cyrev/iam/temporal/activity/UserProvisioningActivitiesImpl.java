package com.cyrev.iam.temporal.activity;

import com.cyrev.iam.domain.IdentityStatus;
import com.cyrev.iam.domain.Role;
import com.cyrev.iam.domain.UserCreationDTO;
import com.cyrev.iam.entities.Organization;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.OrganizationRepository;
import com.cyrev.iam.repository.UserRepository;
import com.cyrev.iam.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserProvisioningActivitiesImpl implements UserProvisioningActivities {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public void createUser(UUID userId) {
        // Check if user already exists (idempotent)
        User user = userRepository.findById(userId).orElse(null);
        if (userRepository.existsByEmail(user.getEmail())) {
            log.info("User with email {} already exists, skipping creation", user.getEmail());
            return;
        }

        Organization organization = organizationRepository.findByCode(user.getOrganization().getCode())
                .orElseThrow(()-> new IllegalStateException("Organization with code " + user.getOrganization().getCode() + " does not exist"));

        user.setOrganization(organization);

        userRepository.save(user);

        log.info("Created user {} {}", user.getFirstName(), user.getLastName());
    }


    @Override
    @Transactional
    public void assignEmployeeId( UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        String organizationCode = user.getOrganization().getCode();
        // Example: employeeId = orgCode + incremental number
        long count = userRepository.countByOrganization_Code(user.getOrganization().getCode());
        String employeeId =  String.format("%s-%05d",organizationCode, count + 1);
        user.setEmployeeId(employeeId);

        userRepository.save(user);

        log.info("Assigned employeeId {} to user {}", employeeId, userId);
    }

    @Override
    @Transactional
    public void assignManager(UUID userId, UUID managerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));

        user.setManager(manager);
        userRepository.save(user);

        log.info("Assigned manager {} to user {}", managerId, userId);
    }

    @Override
    @Transactional
    public void setIdentityStatus(UUID userId, IdentityStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIdentityStatus(status);
        userRepository.save(user);

        log.info("Set identity status {} for user {}", status, userId);
    }

    @Override
    public void notifyUserCreation(UUID userId) {
        notificationService.sendWelcomeNotification(userId);
        log.info("Sent welcome notification to user {}", userId);
    }

    @Transactional
    @Override
    public void activateUser(UUID userId) {

        User user = userRepository.findById(userId).orElseThrow();
        String password = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        notificationService.sendUserActivated(user.getEmail(), user.getFirstName(), password);
    }
}
