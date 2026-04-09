package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.AuthResponse;
import com.cyrev.common.dtos.LoginRequest;
import com.cyrev.common.dtos.UserUpdateRequestDTO;
import com.cyrev.iam.service.EmailVerificationService;
import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.iam.service.AuthService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import static reactor.netty.http.HttpConnectionLiveness.log;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    @Value("${app.base-url}")
    private String appBaseUrl;

    @PostMapping("/login")
    public ResponseEntity<CyrevApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        var response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Login Successful",
                        response
                ));
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) throws ParseException {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logout/full")
    public ResponseEntity<Void> logoutFull(HttpServletRequest request) {

        String logoutUrl = authService.logout();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(logoutUrl))
                .build();
    }

    @PostMapping("/mfa/register")
    @PreAuthorize("hasRole('MFA_WRITE')")
    public ResponseEntity<CyrevApiResponse<Map<String,String>>> register(@CurrentUserId UUID userId) throws IOException {
        try {
            Map<String,String> qrFile = authService.registerMfa(userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CyrevApiResponse<>(
                            true,
                            "MFA Registered Successful",
                            qrFile
                    ));
        } catch (QrGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/mfa/verify")
    @PreAuthorize("hasRole('MFA_WRITE')")
    public ResponseEntity<CyrevApiResponse<AuthResponse>> verifyMFA(@CurrentUserId UUID userId, @RequestParam String code) {
        AuthResponse authResponse =  authService.verifyMFACode(userId, code);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "MFA verified",
                        authResponse
                ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<CyrevApiResponse<String>> verifyEmail(@Valid @RequestBody UserUpdateRequestDTO request) {
        emailVerificationService.verifyToken(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Email verified",
                        "Email verified successfully"
                ));
    }

    @GetMapping("/login")
    public ResponseEntity<CyrevApiResponse<String>> login() {
        String redirectUrl = authService.buildLoginUrl(false, false);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Authentication provider URL retrieved",
                        redirectUrl
                ));
    }
    @GetMapping("/signup")
    public ResponseEntity<CyrevApiResponse<String>> signUp() {
        String redirectUrl = authService.buildLoginUrl(false, true);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Authentication provider URL retrieved",
                        redirectUrl
                ));
    }

    @SneakyThrows
    @GetMapping("/login-callback")
    public void callback(@RequestParam String code, HttpServletResponse response) {
        try{
            AuthResponse authResponse = authService.providerLoginAuth(code);
            String redirectUrl = appBaseUrl +"/login?accessToken=" + authResponse.getAccessToken();
            response.sendRedirect(redirectUrl);
        }
        catch (Exception ex) {
            log.error("OAuth callback failed", ex);
            response.sendRedirect(appBaseUrl +"?error=auth_failed");
        }
    }

    @SneakyThrows
    @GetMapping("/signup-callback")
    public void signupCallback(@RequestParam String code, HttpServletResponse response) {
        try{
            AuthResponse authResponse = authService.providerSignUpAuth(code);
            String redirectUrl = appBaseUrl +"/signup?accessToken=" + authResponse.getAccessToken();
            response.sendRedirect(redirectUrl);
        }
        catch (Exception ex) {
            log.error("OAuth callback failed", ex);
            response.sendRedirect(appBaseUrl +"?error=auth_failed");
        }
    }
}
