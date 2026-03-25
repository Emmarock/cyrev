package com.cyrev.iam.service;

import com.cyrev.common.dtos.AuthResponse;
import com.cyrev.common.dtos.LoginRequest;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.iam.config.EntraProperties;
import com.cyrev.iam.entra.service.clients.MicrosoftGraphClient;
import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final NotificationPublisherService notificationPublisherService;
    private final MFAService mfaService;
    static String appName = "CyRevApp";
    private final EntraProperties props;
    private final MicrosoftGraphClient microsoftGraphClient;

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
        String token = jwtTokenProvider.generateMFAToken(user);
        return new AuthResponse(
                token,
                user.getAuthProvider(),
                user.getId(),
                user.getUsername(),
                user.getTenant().getId().toString(),
                user.isMfaEnabled()
        );
    }

    private AuthResponse issueFullAccessToken( boolean sendEmail, User user) {
        String token = jwtTokenProvider.generateToken(user);
        if (sendEmail) {
            notificationPublisherService.sendWelcomeEmail(user.getFirstName(), user.getEmail());
        }
        return new AuthResponse(
                token,
                user.getAuthProvider(),
                user.getId(),
                user.getUsername(),
                user.getTenant()!=null? user.getTenant().getId().toString(): null,
                user.isMfaEnabled()
        );
    }

    private User getUserByUUID(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: "));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));
    }

    public Map<String,String> registerMfa(UUID userId) throws QrGenerationException, IOException {
        User user = getUserByUUID(userId);
        String secret = mfaService.generateSecretKey();
        user.setSecret(secret);
        userRepository.save(user);
        byte [] qrCodeByte = mfaService.generateQRCode(user.getEmail(), user.getUsername(), appName, secret);
        return Map.of(
                "username", user.getUsername(),
                "secret", secret,
                "qrCode", "data:image/png;base64," +  Base64.getEncoder().encodeToString(qrCodeByte)
        );
    }

    public AuthResponse verifyMFACode(UUID userId, String code) {
        User user = getUserByUUID(userId);
        boolean mfaVerified = mfaService.verifyCode(user.getSecret(), code);
        if (mfaVerified) {
            user.setMfaEnabled(true);
            userRepository.save(user);
            return issueFullAccessToken(true, user);
        }
        throw new UsernameNotFoundException("Invalid MFA code");
    }

    public String buildLoginUrl() {
        String scope = URLEncoder.encode("openid profile email User.Read", StandardCharsets.UTF_8);

        return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
                + "?client_id=" + props.getClientId()
                + "&response_type=code"
                + "&redirect_uri=" + props.getAuthRedirectUri()
                + "&scope="+scope
                + "&response_mode=query"
                + "&state=" + UUID.randomUUID();
    }

    public AuthResponse providerAuth(String code){
        try{
            User user = microsoftGraphClient.handleLoginCallback(code);
            String token;
            if (!user.isMfaEnabled()) {
                token = jwtTokenProvider.generateMFAToken(user);
            }else{
                token = jwtTokenProvider.generateToken(user);
            }
            return new AuthResponse(
                    token,
                    user.getAuthProvider(),
                    user.getId(),
                    user.getUsername(),
                    user.getTenant()!=null? user.getTenant().getId().toString(): null,
                    user.isMfaEnabled()
            );
        }catch(Exception e){
            log.error("Provider authentication failed: {}", e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }
    }
}

