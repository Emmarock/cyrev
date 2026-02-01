package com.cyrev.iam.repository;

import com.cyrev.iam.entities.Organization;
import com.cyrev.iam.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByExternalId(String externalId);
    Optional<User> findByUsername(String username);
    List<User> findAllByIdIn(Set<UUID> userIds);
    Optional<User> findByManagerId(UUID managerId);

    long countByOrganization_Code(String organizationCode);

    boolean existsByEmail(String email);
}
