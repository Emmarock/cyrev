package com.cyrev.common.repository;

import com.cyrev.common.dtos.InviteStatus;
import com.cyrev.common.entities.UserInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserInviteRepository extends JpaRepository<UserInvite, UUID> {

    Optional<UserInvite> findByInviteTokenAndDeletedFalse(String token);

    boolean existsByEmailAndStatus(String email, InviteStatus status);
}