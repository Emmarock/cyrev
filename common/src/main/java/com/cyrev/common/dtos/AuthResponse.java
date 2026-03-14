package com.cyrev.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private AuthProvider authProvider;
    private UUID userId;
    private String username;
    private String orgId;
    private boolean isMfaEnabled;
}
