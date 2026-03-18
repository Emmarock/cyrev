package com.cyrev.iam.entra.service.clients;

import com.cyrev.common.dtos.AuthProvider;
import com.cyrev.common.dtos.EntraTokenResponse;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.iam.entra.service.onboarding.TenantAccessTokenService;
import com.cyrev.iam.service.UserMapper;
import com.google.zxing.NotFoundException;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MicrosoftGraphClient {

    private final WebClient webClient = WebClient.create();
    private final TenantAccessTokenService tokenService;
    private final UserRepository userRepository;
    private final SaasTenantRepository saasTenantRepository;

    public String verifyTenant(String tenantId) {

        String token = tokenService.getTenantUserAccessToken(tenantId);

        Map<String,Object> response = webClient.get()
                .uri("https://graph.microsoft.com/v1.0/organization")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String,Object>> value = (List<Map<String,Object>>) response.get("value");

        return (String) value.get(0).get("displayName");
    }

    public User getUserProfile(String code) throws ParseException {
        EntraTokenResponse tokenResponse = tokenService.getTenantUserAccessTokenFromCode(code);

        Map<String,Object> response = webClient.get()
                .uri("https://graph.microsoft.com/v1.0/me")
                .headers(h -> h.setBearerAuth(tokenResponse.getAccessToken()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        SignedJWT jwt = (SignedJWT) JWTParser.parse(tokenResponse.getIdToken());  // using Nimbus JWT library
        String tenantId = jwt.getJWTClaimsSet().getStringClaim("tid"); // "tid" claim has the tenant id
        SaasTenant saasTenant = saasTenantRepository.findByEntraTenantId(tenantId)
                .orElse(null);
        String microsoftId = response.get("id").toString();
        String email = response.get("mail") != null ? response.get("mail").toString() : response.get("userPrincipalName").toString();
        String surname = response.get("surname").toString();
        String givenName = response.get("givenName").toString();

        Optional<User> existing =
                userRepository.findByAuthProviderAndProviderUserId(
                        AuthProvider.MICROSOFT,
                        microsoftId
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(givenName);
        user.setLastName(surname);
        user.setUsername(UserMapper.emailToUsername(email));
        user.setAuthProvider(AuthProvider.MICROSOFT);
        user.setOrganization(saasTenant.getOrganization());
        user.setEmailVerified(true);
        user.setRole(Role.MFA_WRITE);
        user.setProviderUserId(microsoftId);

        return userRepository.save(user);
    }
}