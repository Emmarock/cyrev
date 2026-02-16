package com.cyrev.iam.service;

import com.cyrev.common.dtos.UserStatus;
import com.cyrev.common.dtos.UserUpdateRequestDTO;
import com.cyrev.common.entities.EmailVerificationToken;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.EmailVerificationTokenRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.VerificationTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final VerificationTokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public String generateVerificationLink(User user) {

        String rawToken = tokenGenerator.generateToken();

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .token(rawToken)
                        .user(user)
                        .expiryDate(Instant.now().plusSeconds(3600)) // 1 hr
                        .used(false)
                        .build();

        tokenRepository.save(verificationToken);

        return baseUrl + "/set-password?token=" + rawToken;
    }

    public void verifyToken(UserUpdateRequestDTO request) {

        EmailVerificationToken verificationToken =
                tokenRepository.findByToken(request.getSecret())
                        .orElseThrow(() ->
                                new RuntimeException("Invalid token"));

        if (verificationToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Token expired");
        }
        verifyUser(verificationToken.getUser(), request);
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);
    }


    private void verifyUser(User user, UserUpdateRequestDTO updated) {
        user.setPassword(passwordEncoder.encode(updated.getPassword()));
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
