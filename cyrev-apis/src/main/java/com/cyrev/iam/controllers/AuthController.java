package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.AuthResponse;
import com.cyrev.common.dtos.LoginRequest;
import com.cyrev.common.dtos.UserUpdateRequestDTO;
import com.cyrev.iam.service.EmailVerificationService;
import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.iam.service.AuthService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

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

}
