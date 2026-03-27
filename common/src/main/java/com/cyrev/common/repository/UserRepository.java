package com.cyrev.common.repository;

import com.cyrev.common.dtos.AuthProvider;
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


    boolean existsByEmail(String email);
    Optional<User> findByAuthProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );


    Optional<User> findUserByIdAndTenant_Id(UUID id, UUID tenantId);
    Optional<User> findUserByEmailAndTenant_Id(String email, UUID tenantId);
}
