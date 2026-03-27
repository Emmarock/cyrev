package com.cyrev.iam.entra.service.clients;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.iam.entra.service.EntraOrganizationService;
import com.cyrev.iam.entra.service.onboarding.TenantAccessTokenService;
import com.cyrev.iam.entra.service.onboarding.SaasTenantService;
import com.cyrev.iam.exceptions.BadRequestException;
import com.cyrev.iam.service.UserMapper;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MicrosoftGraphClient {

    private final UserRepository userRepository;
    private final SaasTenantService saasTenantService;
    private final ResilientGraphClient resilientGraphClient;
    private final TenantAccessTokenService tokenService;
    private final EntraOrganizationService entraOrganizationService;

    public String verifyTenant(String tenantId) {
        return entraOrganizationService.verifyTenant(tenantId).getDisplayName();
    }

    public User handleLoginCallback(String code) throws ParseException {
        EntraTokenResponse tokenResponse = tokenService.getTenantUserAccessTokenFromCode(code);

        SignedJWT jwt = (SignedJWT) JWTParser.parse(tokenResponse.getIdToken());  // using Nimbus JWT library
        String tenantId = jwt.getJWTClaimsSet().getStringClaim("tid"); // "tid" claim has the tenant id
        SaasTenant tenant = saasTenantService.findTenant(UUID.fromString(tenantId));
        if (tenant == null) {
            throw new BadRequestException(
                    "Your organization has not been onboarded. Contact your admin. If you are an admin ensure consent has been given"
            );
        }
        EntraUser entraUser = getUserProfile(tokenResponse.getAccessToken(), code);
        if(tenant.getStatus()!= TenantStatus.ACTIVE){
            saasTenantService.activateTenant(UUID.fromString(tenantId));
        }
        Optional<User> existing = userRepository.findByAuthProviderAndProviderUserIdAndEmail(AuthProvider.MICROSOFT, entraUser.getId(),entraUser.getMail());
        log.info("Existing user found: {}", existing.isPresent());
        return existing.orElseGet(() -> createUser(entraUser, tenant));
    }

    @NotNull
    private User createUser(EntraUser entraUser, SaasTenant tenant) {

        User user = new User();
        user.setEmail(entraUser.getMail());
        user.setFirstName(entraUser.getGivenName());
        user.setLastName(entraUser.getFamilyName());
        user.setUsername(UserMapper.emailToUsername(entraUser.getMail()));
        user.setAuthProvider(AuthProvider.MICROSOFT);
        user.setTenant(tenant);
        user.setEmailVerified(true);
        user.setRole(Role.MFA_WRITE);
        user.setProviderUserId(entraUser.getId());
        return userRepository.save(user);
    }

    private EntraUser mapToEntraUser(Map<String, Object> response) {
        return EntraUser.builder()
                .id((String) response.get("id"))
                .mail(response.get("mail") == null?(String) response.get("userPrincipalName"): (String) response.get("mail"))
                .userPrincipalName((String) response.get("userPrincipalName"))
                .givenName((String) response.get("givenName"))
                .familyName((String) response.get("surname"))
                .build();
    }
    public EntraUser getUserProfile(String accessToken, String code) {
        Map<String, Object> response =resilientGraphClient.getUserProfile(accessToken,code, "/me");
        return mapToEntraUser(response);
    }
}