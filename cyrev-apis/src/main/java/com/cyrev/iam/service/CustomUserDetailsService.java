package com.cyrev.iam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    @Value("${cyrev.admin}")
    private String cyrevAdmin;
    @Value("${cyrev.secret}")
    private String cyrevSecret;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // For testing, we just create a static admin user
        if (cyrevAdmin.equals(username)) {
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );
            String encodedPassword = passwordEncoder.encode(cyrevSecret);
            return org.springframework.security.core.userdetails.User
                    .withUsername(cyrevAdmin)
                    .password(encodedPassword)
                    .authorities(authorities)
                    .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
