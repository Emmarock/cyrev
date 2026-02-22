package com.cyrev.iam.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private UUID userId;
    private String username;
    private String orgName;
    private Collection<? extends GrantedAuthority> authorities;
}
