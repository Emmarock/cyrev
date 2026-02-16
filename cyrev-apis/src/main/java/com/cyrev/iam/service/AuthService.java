package com.cyrev.iam.service;

import com.cyrev.common.dtos.AuthResponse;
import com.cyrev.common.dtos.LoginRequest;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final NotificationPublisherService notificationPublisherService;
    private final MFAService mfaService;
    static String appName = "CyRevApp";

    public AuthResponse login(LoginRequest request) {
       return login(request,true);
    }

    public AuthResponse login(LoginRequest request, boolean sendEmail) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = getUser(request.getEmail());
        if(user.isMfaEnabled()){
            String token = jwtTokenProvider.generateMFAToken(user);
            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getOrganization().getCode()
            );
        }else {
            return issueFullAccessToken( sendEmail, user);
        }
    }

    private AuthResponse issueFullAccessToken( boolean sendEmail, User user) {
        String token = jwtTokenProvider.generateToken(user);
        if (sendEmail) {
            notificationPublisherService.sendWelcomeEmail(user.getFirstName(), user.getEmail());
        }
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getOrganization().getCode()
        );
    }

    private User getUserByUUID(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: "));
    }

    private User getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));
        return user;
    }

    public Map<String,String> registerMfa(UUID userId) throws QrGenerationException, IOException {
        User user = getUserByUUID(userId);
        String secret = mfaService.generateSecretKey();
        user.setSecret(secret);
        userRepository.save(user);
        byte [] qrCodeByte = mfaService.generateQRCode(user.getUsername(), appName, secret);
        return Map.of(
                "username", user.getUsername(),
                "secret", secret,
                "qrCode", "data:image/png;base64," +  Base64.getEncoder().encodeToString(qrCodeByte)
        );
    }

    public AuthResponse verifyMFACode(UUID emailAddress, String code) {
        User user = getUserByUUID(emailAddress);
        boolean mfaVerified = mfaService.verifyCode(user.getSecret(), code);
        if (mfaVerified) {
            user.setMfaEnabled(true);
            userRepository.save(user);
            return issueFullAccessToken(true, user);
        }
        throw new UsernameNotFoundException("Invalid MFA code");
    }
}

