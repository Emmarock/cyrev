package com.cyrev.iam.service;

import com.cyrev.common.dtos.AuthResponse;
import com.cyrev.common.dtos.LoginRequest;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.common.services.NotificationPublisherService;
import com.cyrev.iam.config.EntraProperties;
import com.cyrev.iam.entra.service.clients.MicrosoftGraphClient;
import dev.samstevens.totp.exceptions.QrGenerationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
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
    private final TokenBlacklistService tokenBlacklistService;
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
        AuthResponse authResponse = getAuthResponse(token, user);
        if (sendEmail) {
            notificationPublisherService.publishLoginEvent(user.getFirstName(), user.getEmail());
        }
        return authResponse;
    }

    public void logout(HttpServletRequest request) {
        // 1. Extract access token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claims = jwtTokenProvider.parseClaims(token);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();
            long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            if (ttl > 0) {
                tokenBlacklistService.blacklistToken(jti, ttl);
            }
        }

        // 3. Clear Spring Security
        SecurityContextHolder.clearContext();
    }

    public String logout(){
        // IMPORTANT: keeps encoding safe
        String tenantId = TenantContextHolder.get().getEntraTenantId();
        return UriComponentsBuilder
                .fromHttpUrl("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/logout")
                .queryParam("post_logout_redirect_uri", props.getLoginRedirectUri())
                .build(true) // IMPORTANT: keeps encoding safe
                .toUriString();
    }

    @NotNull
    private static AuthResponse getAuthResponse(String token, User user) {
        SaasTenant saasTenant = user.getTenant();
        return new AuthResponse(
                token,
                user.getAuthProvider(),
                user.getId(),
                user.getUsername(),
                user.getTenant() != null ? user.getTenant().getId().toString() : null,
                user.isMfaEnabled(),
                saasTenant != null && saasTenant.isConsentGranted()
        );
    }

    private AuthResponse issueFullAccessToken( boolean sendEmail, User user) {
        String token = jwtTokenProvider.generateToken(user);
        if (sendEmail) {
            notificationPublisherService.publishSignupEvent(user.getFirstName(), user.getEmail());
        }
        return getAuthResponse(token, user);
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

    public String buildLoginUrl(boolean connectingEntra, boolean isSignup) {
        String scope = URLEncoder.encode("openid profile email User.Read", StandardCharsets.UTF_8);
        String redirectUrl = isSignup ? props.getSignupRedirectUri(): props.getLoginRedirectUri();
        String defaultUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
                + "?client_id=" + props.getAppId()
                + "&response_type=code"
                + "&redirect_uri=" + redirectUrl
                + "&scope="+scope
                + "&response_mode=query"
                + "&state=" + UUID.randomUUID();
        if (connectingEntra) {
            defaultUrl += "&connectingEntra=true";
        }
        return defaultUrl;
    }

    public AuthResponse providerLoginAuth(String code){
        try{
            User user = microsoftGraphClient.handleLoginCallback(code);
            String token;
            if (!user.isMfaEnabled()) {
                token = jwtTokenProvider.generateMFAToken(user);
            }else{
                token = jwtTokenProvider.generateToken(user);
            }
            return getAuthResponse(token, user);
        }catch(Exception e){
            log.error("Provider login authentication failed: {}", e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }
    }

    public AuthResponse providerSignUpAuth(String code){
        try{
            User user = microsoftGraphClient.handleSignupCallback(code);
            String token = jwtTokenProvider.generateToken(user);
            return getAuthResponse(token, user);
        }catch(Exception e){
            log.error("Provider signup authentication failed: {}", e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }
    }
}

