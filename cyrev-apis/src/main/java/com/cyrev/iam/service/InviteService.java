package com.cyrev.iam.service;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.User;
import com.cyrev.common.entities.UserInvite;
import com.cyrev.common.repository.UserInviteRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.common.services.VerificationTokenGenerator;
import com.cyrev.iam.exceptions.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    //TODO: allow user invitation from users who do not have tenant connected
    // to entra to invite other users, who can then connect to entra

    private UserInviteDTO toDto(UserInvite invite) {
        return UserInviteDTO.builder()
                .firstName(invite.getFirstName())
                .lastName(invite.getLastName())
                .email(invite.getEmail())
                .role(invite.getRole())
                .status(invite.getStatus())
                .expiresAt(invite.getExpiresAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserInviteDTO> listPendingInvites(UUID tenantId) {
        return inviteRepository
                .findAllByInviter_Tenant_IdAndStatusNotOrderByCreatedAtDesc(tenantId, InviteStatus.ACCEPTED)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserInviteDTO sendInvite(UUID inviter, InviteUserRequest request) {

        User user = userRepository.findById(inviter).orElseThrow(()-> new EntityNotFoundException("User not found"));
        String normalizedEmail = request.getBusinessEmail().toLowerCase();

        if (inviteRepository.existsByEmailAndStatus(normalizedEmail, InviteStatus.PENDING)) {
            throw new BadRequestException("An invitation has already been sent to this email");
        }

        userRepository.findByEmail(normalizedEmail).ifPresent(existing -> {
            if (existing.getStatus() == UserStatus.ACTIVE) {
                throw new BadRequestException("A user with this email is already registered and active");
            }
            if (existing.getStatus() == UserStatus.SUSPENDED
                    || existing.getStatus() == UserStatus.INACTIVE
                    || existing.getStatus() == UserStatus.TERMINATED) {
                throw new BadRequestException("A user with this email exists but their account is disabled");
            }
        });

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
        String invitationLink = emailVerificationService.getInvitationLink(verificationToken);
        notificationPublisherService.publishVerificationEvent(request.getFirstName(), request.getBusinessEmail(), invitationLink);
        return toDto(invite);
    }

    @Transactional
    public UserInviteDTO resendInvite(String email) {
        UserInvite invite = inviteRepository.findFirstByEmailOrderByCreatedAtDesc(email.toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("No invite found for: " + email));

        if (invite.getStatus() == InviteStatus.ACCEPTED) {
            throw new BadRequestException("Invite already accepted — user has completed registration");
        }
        if (invite.getStatus() == InviteStatus.REVOKED) {
            throw new BadRequestException("Invite has been revoked");
        }

        String newToken = verificationTokenGenerator.generateToken();
        invite.setInviteToken(newToken);
        invite.setStatus(InviteStatus.PENDING);
        invite.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        inviteRepository.save(invite);

        String invitationLink = emailVerificationService.getInvitationLink(newToken);
        notificationPublisherService.publishVerificationEvent(invite.getFirstName(), invite.getEmail(), invitationLink);
        return toDto(invite);
    }

    public AcceptInviteDTO acceptInvite(AcceptInviteRequest request) {

        UserInvite invite = inviteRepository
                .findByInviteTokenAndDeletedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid invite token"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invite is no longer valid");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new BadRequestException("Invite has expired");
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
        user.setTenant(inviter.getTenant()!=null? inviter.getTenant() :  null);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
        return AcceptInviteDTO.builder()
                .inviteStatus(invite.getStatus())
                .message("User created successfully").build();
    }
}