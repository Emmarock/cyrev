package com.cyrev.iam.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private UUID userId;
    private String username;
    private String orgCode;
}
