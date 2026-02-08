package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.AuthResponse;
import com.cyrev.common.dtos.LoginRequest;
import com.cyrev.iam.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}
