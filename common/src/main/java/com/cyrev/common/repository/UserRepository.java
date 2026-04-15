package com.cyrev.common.repository;

import com.cyrev.common.dtos.AuthProvider;
import com.cyrev.common.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findAllByIdIn(Set<UUID> userIds);
    List<User> findAllByTenantId(UUID tenantId);


    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain)")
    Optional<User> findByEmailDomain(@Param("domain") String domain);


    Optional<User> findUserByIdAndTenant_Id(UUID id, UUID tenantId);
    Optional<User> findByEmailAndTenant_Id(String email, UUID tenantId);
}
