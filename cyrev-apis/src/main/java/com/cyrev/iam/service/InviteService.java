package com.cyrev.iam.service;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.common.entities.User;
import com.cyrev.common.entities.UserInvite;
import com.cyrev.common.repository.UserInviteRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.common.services.VerificationTokenGenerator;
import com.cyrev.iam.filters.TenantContextFilter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final UserInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationPublisherService notificationPublisherService;
    private final EmailVerificationService emailVerificationService;
    private final VerificationTokenGenerator verificationTokenGenerator;

    public UserInviteDTO sendInvite(UUID inviter, InviteUserRequest request) {
        TenantContext tenantContext = TenantContextHolder.get();
        String entraTenantId = tenantContext.getEntraTenantId();
        User user = userRepository.findById(inviter).orElseThrow(()-> new EntityNotFoundException("User not found"));
        if (inviteRepository.existsByEmailAndStatus(request.getBusinessEmail(), InviteStatus.PENDING)) {
            throw new RuntimeException("User already invited");
        }
        if(user.getTenant()==null || entraTenantId==null){
            throw new RuntimeException("User tenant can not be null");
        }
        String verificationToken = verificationTokenGenerator.generateToken();
        UserInvite invite = UserInvite.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .inviter(user)
                .email(request.getBusinessEmail().toLowerCase())
                .role(request.getRole())
                .inviteToken(verificationToken)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(InviteStatus.PENDING)
                .deleted(false)
                .build();
        inviteRepository.save(invite);
        String verificationLink = emailVerificationService.getVerificationLink(verificationToken);
        notificationPublisherService.publishVerificationEvent(request.getFirstName(), request.getBusinessEmail(), verificationLink);
        return UserInviteDTO.builder()
                .firstName(invite.getFirstName())
                .lastName(invite.getLastName())
                .email(invite.getEmail())
                .role(invite.getRole())
                .build();
    }

    public AcceptInviteDTO acceptInvite(AcceptInviteRequest request) {

        UserInvite invite = inviteRepository
                .findByInviteTokenAndDeletedFalse(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid invite"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite already used");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new RuntimeException("Invite expired");
        }
        User inviter = invite.getInviter();
        User user = new User();
        user.setFirstName(invite.getFirstName());
        user.setLastName(invite.getLastName());
        user.setUsername(UserMapper.emailToUsername(invite.getEmail()));
        user.setEmail(invite.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAuthProvider(AuthProvider.CYREV);
        user.setRole(invite.getRole());
        user.setTenant(inviter.getTenant());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
        return AcceptInviteDTO.builder().inviteStatus(invite.getStatus()).message("User created successfully").build();
    }
}