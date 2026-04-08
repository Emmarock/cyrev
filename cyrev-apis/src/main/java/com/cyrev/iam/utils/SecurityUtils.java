package com.cyrev.iam.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityUtils {
    private SecurityUtils() {
        // prevent instantiation
    }

    public static String getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // If you are using JwtAuthenticationToken
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Object tenantClaim = jwtAuth.getToken().getClaim("tid"); // depends on your JWT claim name
            return tenantClaim != null ? tenantClaim.toString() : null;
        }

        return null;
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName(); // default Spring Security username
    }
}