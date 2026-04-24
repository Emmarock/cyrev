package com.cyrev.iam.entra.service.clients;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.iam.entra.service.onboarding.ConsentStateService;
import com.cyrev.iam.entra.service.onboarding.TenantAccessTokenService;
import com.cyrev.iam.entra.service.onboarding.SaasTenantService;
import com.cyrev.iam.entra.service.utils.StatePayload;
import com.cyrev.iam.exceptions.BadRequestException;
import com.cyrev.iam.service.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Base64;
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
    private final ConsentStateService consentStateService;
    private final ObjectMapper objectMapper;

    //TODO: find out what happens if the user who is signing up is not an admin of the cyrev
    public User handleSignupCallback(String code) throws ParseException, JsonProcessingException {
        String state = consentStateService.generate();
        StatePayload payload = new StatePayload(
                LocalDateTime.now().plusMinutes(10),
                state
        );
        String stateJson = objectMapper.writeValueAsString(payload);
        String stateEncoded = Base64.getUrlEncoder()
                .encodeToString(stateJson.getBytes(StandardCharsets.UTF_8));
        EntraTokenResponse tokenResponse = tokenService.getSignupAccessTokenFromCode(code);
        log.info("signup token response={}", tokenResponse);
        String tenantId = getTenantIdFromMicrosoftToken(tokenResponse.getIdToken());
        log.info("tenantId={} received from microsoft", tenantId);
        EntraUser entraUser = getUserProfile(tokenResponse.getAccessToken(), code);
        if(userRepository.findByEmailDomain(entraUser.getMail().split("@")[1]).isPresent()) {
            throw new BadRequestException("Your organization already exists in cyrev, please contact our customer support for more information.");
        }
        SaasTenant tenant = saasTenantService.registerTenant(stateEncoded, UUID.fromString(tenantId), false);
        log.info("tenant={} registered successfully", tenant);
        return createUser(entraUser, tenant);
    }

    public User createUserOnSignIn(String tokenId, EntraUser entraUser) throws ParseException, JsonProcessingException {
        String state = consentStateService.generate();
        StatePayload payload = new StatePayload(
                LocalDateTime.now().plusMinutes(10),
                state
        );
        String stateJson = objectMapper.writeValueAsString(payload);
        String stateEncoded = Base64.getUrlEncoder()
                .encodeToString(stateJson.getBytes(StandardCharsets.UTF_8));
        String tenantId = getTenantIdFromMicrosoftToken(tokenId);
        SaasTenant tenant = saasTenantService.registerTenant(stateEncoded, UUID.fromString(tenantId), false);
        return createUser(entraUser, tenant);
    }

    private static String getTenantIdFromMicrosoftToken(String tokenId) throws ParseException {
        SignedJWT jwt = (SignedJWT) JWTParser.parse(tokenId);  // using Nimbus JWT library
        return jwt.getJWTClaimsSet().getStringClaim("tid");     // "tid" claim has the tenant id
    }

    public User handleLoginCallback(String code) {
        EntraTokenResponse tokenResponse = tokenService.getLoginAccessTokenFromCode(code);
        log.info("login token response={}", tokenResponse);
        EntraUser entraUser = getUserProfile(tokenResponse.getAccessToken(), code);
        Optional<User> existing = userRepository.findByEmail(entraUser.getMail());
        log.info("Existing user found: {}", existing.isPresent());
        return existing.orElseThrow(
                ()-> new BadRequestException("You need an administrator invite to login to cyrev")
        );
    }

    @NotNull
    private User createUser(EntraUser entraUser, SaasTenant tenant) {
        Optional<User> existing = userRepository.findByEmail(entraUser.getMail());
        User user = existing.orElseGet(User::new);
        user.setEmail(entraUser.getMail());
        user.setFirstName(entraUser.getGivenName());
        user.setLastName(entraUser.getFamilyName());
        user.setUsername(UserMapper.emailToUsername(entraUser.getMail()));
        user.setAuthProvider(user.getAuthProvider());
        user.setTenant(tenant);
        user.setEmailVerified(true);
        user.setMfaEnabled(false);
        user.setStatus(UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.MICROSOFT);
        user.setRole(Role.SUPER_ADMIN);
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