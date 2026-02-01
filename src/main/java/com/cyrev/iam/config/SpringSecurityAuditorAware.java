
package com.cyrev.iam.config;

import com.cyrev.iam.domain.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth instanceof UsernamePasswordAuthenticationToken) {
            AuthenticatedUser principal = (AuthenticatedUser) auth.getPrincipal();
            return Optional.ofNullable(principal.getUsername());
        }
        return Optional.ofNullable("System");
    }
}
