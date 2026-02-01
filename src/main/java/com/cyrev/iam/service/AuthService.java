package com.cyrev.iam.service;

import com.cyrev.iam.domain.AuthResponse;
import com.cyrev.iam.domain.LoginRequest;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.UserRepository;
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

        // 4️⃣ Return response
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getOrganization().getCode()
        );
    }
}

