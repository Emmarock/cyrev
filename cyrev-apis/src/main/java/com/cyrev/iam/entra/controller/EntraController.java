package com.cyrev.iam.entra.controller;

import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.EntraGroup;
import com.cyrev.common.dtos.EntraUser;
import com.cyrev.common.entities.SaasTenant;
import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.entra.service.*;
import com.cyrev.iam.entra.service.onboarding.ConsentStateService;
import com.cyrev.iam.entra.service.onboarding.EntraConsentService;
import com.cyrev.iam.entra.service.onboarding.TenantOnboardingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.requests.UserCollectionPage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cyrev")
@RequiredArgsConstructor
public class EntraController {

    private final EntraUserService entraUserService;
    private final EntraGroupService entraGroupService;
    private final ApplicationService appService;
    private final EntraConsentService consentService;
    private final TenantOnboardingService onboardingService;

    @GetMapping("/connect-entra")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<String>> connect(@CurrentUserId UUID adminId) {
        String url = consentService.buildUrl(adminId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Redirect URL Retrieved",
                        url
                ));
    }

    @GetMapping("/admin-consent-callback")
    public ResponseEntity<CyrevApiResponse<SaasTenant>> callback(@RequestParam UUID tenant, @RequestParam String state, @RequestParam(required = false) String admin_consent) throws JsonProcessingException {
        if (!"True".equalsIgnoreCase(admin_consent)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CyrevApiResponse<>(
                            true,
                            "Entra Tenant Denied",
                            null
                    ));
        }
        String decoded = new String(
                Base64.getUrlDecoder().decode(state),
                StandardCharsets.UTF_8
        );
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(decoded);

        UUID orgId = UUID.fromString(node.get("orgId").asText());
        String originalState = node.get("state").asText();
        SaasTenant saasTenant = onboardingService.registerTenant(orgId, originalState, tenant);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Entra Tenant Consent Accepted",
                        saasTenant
                ));
    }

    @PostMapping("/users")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<EntraUser>> createUser(@RequestBody EntraUser entraUser) {
        var response = entraUserService.createUser(entraUser.getDisplayName(), entraUser.getMail(), entraUser.getUserPrincipalName(), entraUser.getPassword());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Entra User List Retrieved",
                        response
                ));
    }

    @GetMapping("/users")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<List<EntraUser>>> listUsers() {
        var response =  entraUserService.listUsers();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Entra User List Retrieved",
                        response
                ));
    }

    @PostMapping("/groups")
    @TenantAdmin
    public Object createGroup(@RequestBody EntraGroup entraGroup) {
        return entraGroupService.createGroup(entraGroup);
    }

    @PostMapping("/applications")
    public Object createApp(@RequestParam String displayName) {
        return appService.createApplication(displayName);
    }
}
