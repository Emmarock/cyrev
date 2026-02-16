package com.cyrev.common.repository;

import com.cyrev.common.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findAllByIdIn(Set<UUID> userIds);

    long countByOrganization_Code(String organizationCode);

    boolean existsByEmail(String email);
}
