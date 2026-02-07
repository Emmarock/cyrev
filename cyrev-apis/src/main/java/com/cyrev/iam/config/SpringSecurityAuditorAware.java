
package com.cyrev.iam.config;

import com.cyrev.iam.domain.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        // JWT-authenticated user
        if (principal instanceof AuthenticatedUser user) {
            return Optional.of(user.getUsername());
        }

        // Basic-authenticated user
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        }

        // Fallback (rare but safe)
        if (principal instanceof String str) {
            return Optional.of(str);
        }

        return Optional.of("SYSTEM");
    }
}
