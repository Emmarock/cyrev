package com.cyrev.iam.service;

import com.cyrev.common.dtos.AuthResponse;
import com.cyrev.common.dtos.LoginRequest;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.common.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final NotificationPublisherService notificationPublisherService;

    public AuthResponse login(LoginRequest request) {

        // 1️⃣ Authenticate username & password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + request.getEmail()
                ));

        // 3️⃣ Generate JWT with enriched claims
        String token = jwtTokenProvider.generateToken(user);
        notificationPublisherService.sendWelcomeEmail(user.getFirstName(), request.getEmail());
        // 4️⃣ Return response
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getOrganization().getCode()
        );
    }
}

