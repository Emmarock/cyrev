package com.cyrev.common.repository;

import com.cyrev.common.entities.Organization;
import com.cyrev.common.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    boolean existsByName(String name);

    Optional<Organization> findByName( String name);

    Optional<Organization> findByOwners(Set<User> owners);
}
