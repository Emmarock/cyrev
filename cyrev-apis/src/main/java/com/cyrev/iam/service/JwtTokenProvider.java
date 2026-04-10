package com.cyrev.iam.service;

import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiry}")
    private long expiration;

    @Value("${security.jwt.mfa-expiry}")
    private long mfaExpiration;

    public String generateToken(User user, boolean entraConnected) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setId(jti)
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim("tenantId", user.getTenant()!=null?user.getTenant().getId():null)
                .claim("authProvider", user.getAuthProvider())
                .claim("roles", user.getRole()) // ["ADMIN","USER"]
                .claim("isMfaEnabled", user.isMfaEnabled())
                .claim("isEntraConnected", entraConnected)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateMFAToken(User user, boolean entraConnected) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setId(jti)
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("tenantId", user.getTenant()!=null?user.getTenant().getId():null)
                .claim("authProvider", user.getAuthProvider())
                .claim("roles", Role.MFA_WRITE.toString())
                .claim("isMfaEnabled", user.isMfaEnabled())
                .claim("isEntraConnected", entraConnected)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + mfaExpiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
