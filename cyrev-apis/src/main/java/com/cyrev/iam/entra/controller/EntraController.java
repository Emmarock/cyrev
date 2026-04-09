package com.cyrev.iam.entra.controller;

import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.EntraGroup;
import com.cyrev.common.dtos.EntraOrganization;
import com.cyrev.common.dtos.EntraUser;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.entra.service.*;
import com.cyrev.iam.entra.service.onboarding.EntraConsentService;
import com.cyrev.iam.entra.service.onboarding.SaasTenantService;
import com.cyrev.iam.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/entra")
@RequiredArgsConstructor
@Tag(name = "Entra Connection", description = "API for establishing connection to entra")
public class EntraController {

    private final EntraUserService entraUserService;
    private final EntraGroupService entraGroupService;
    private final ApplicationService appService;
    private final EntraConsentService consentService;
    private final SaasTenantService saasTenantService;
    private final EntraOrganizationService organizationService;
    private final AuthService authService;

    @GetMapping("/connect-entra")
    public ResponseEntity<CyrevApiResponse<String>> connect() {
        String url = consentService.buildUrl();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Redirect URL Retrieved",
                        url
                ));
    }

    @GetMapping("/admin-consent-callback")
    public ResponseEntity<Void> callback(@RequestParam UUID tenant, @RequestParam String state, @RequestParam(required = false) String admin_consent) {
        if (!"True".equalsIgnoreCase(admin_consent)) {
            URI deniedRedirect = URI.create("/error?reason=consent_denied");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(deniedRedirect)
                    .build();
        }
        saasTenantService.registerTenant(state, tenant, true);
        String redirectUrl = authService.buildLoginUrl(true, false);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
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

    @GetMapping("/organization")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<EntraOrganization>> getOrganization() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Entra Organization details retrieved",
                        organizationService.getOrganization()
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
