package com.cyrev.iam.service;

import com.cyrev.common.entities.PasswordResetToken;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.PasswordResetTokenRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.common.services.VerificationTokenGenerator;
import com.cyrev.iam.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private static final long EXPIRY_SECONDS = 1800; // 30 minutes
    private static final String INVALID_OR_EXPIRED = "Invalid or expired reset link";

    private final PasswordResetTokenRepository tokenRepository;
    private final VerificationTokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final NotificationPublisherService notificationPublisherService;

    @Value("${app.base-url}")
    private String baseUrl;

    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(this::issueResetToken);
        // Always returns silently regardless of whether the email was found —
        // the controller responds with the same generic message either way.
    }

    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new BadRequestException(INVALID_OR_EXPIRED));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException(INVALID_OR_EXPIRED);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private void issueResetToken(User user) {
        tokenRepository.findAllByUserAndUsedFalse(user)
                .forEach(token -> {
                    token.setUsed(true);
                    tokenRepository.save(token);
                });

        String rawToken = tokenGenerator.generateToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(rawToken)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(EXPIRY_SECONDS))
                .used(false)
                .build();
        tokenRepository.save(resetToken);

        String resetLink = baseUrl + "/reset-password?token=" + rawToken;
        notificationPublisherService.publishPasswordResetEvent(user.getFirstName(), user.getEmail(), resetLink);
        log.info("Issued password reset token for user {}", user.getId());
    }
}
