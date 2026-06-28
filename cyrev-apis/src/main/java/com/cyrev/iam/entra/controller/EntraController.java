package com.cyrev.iam.entra.controller;

import com.cyrev.common.dtos.AccessPackageDto;
import com.cyrev.common.dtos.CyrevApiResponse;
import com.cyrev.common.dtos.EntraGroup;
import com.cyrev.common.dtos.EntraOrganization;
import com.cyrev.common.dtos.EntraUser;
import com.cyrev.common.dtos.ServicePrincipalDto;
import com.cyrev.common.dtos.SharedMailboxDto;
import com.cyrev.iam.annotations.RelationshipManager;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.entra.service.*;
import com.cyrev.iam.entra.service.onboarding.EntraConsentService;
import com.cyrev.iam.entra.service.onboarding.ExchangeBootstrapService;
import com.cyrev.iam.entra.service.onboarding.SaasTenantService;
import com.cyrev.iam.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class EntraController {

    private final EntraUserService entraUserService;
    private final EntraGroupService entraGroupService;
    private final ApplicationService appService;
    private final AccessPackageService accessPackageService;
    private final EntraConsentService consentService;
    private final SaasTenantService saasTenantService;
    private final EntraOrganizationService organizationService;
    private final AuthService authService;
    private final ExchangeBootstrapService exchangeBootstrapService;

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
            log.error("admin has refused to give it's consent");
            URI deniedRedirect = URI.create("/error?reason=consent_denied");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(deniedRedirect)
                    .build();
        }

        saasTenantService.registerTenant(state, tenant, true);
        try {
            exchangeBootstrapService.provisionExchangeAdminRole(tenant.toString());
        } catch (Exception e) {
            log.error("Automatic Exchange Online role provisioning failed for tenant {}; " +
                    "admin can fall back to /api/entra/exchange-setup-script: {}", tenant, e.getMessage());
        }
        String redirectUrl = authService.buildLoginUrl(true, false);
        log.info("Admin has consented :  Redirect URL Retrieved: {}", redirectUrl);
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

    @GetMapping("/groups")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<List<EntraGroup>>> listGroups() {
        List<EntraGroup> groups = entraGroupService.listGroups();
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Groups retrieved", groups));
    }

    @GetMapping("/service-principals")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<List<ServicePrincipalDto>>> listServicePrincipals() {
        List<ServicePrincipalDto> principals = appService.listServicePrincipals();
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Service principals retrieved", principals));
    }

    @GetMapping("/access-packages")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<List<AccessPackageDto>>> listAccessPackages() {
        List<AccessPackageDto> packages = accessPackageService.listAccessPackages();
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Access packages retrieved", packages));
    }

    @GetMapping("/shared-mailboxes")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<List<SharedMailboxDto>>> listSharedMailboxes() {
        List<SharedMailboxDto> mailboxes = entraUserService.listSharedMailboxes();
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Shared mailboxes retrieved", mailboxes));
    }

    @GetMapping("/exchange-setup-script")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<String>> exchangeSetupScript() {
        String script = exchangeBootstrapService.generateSetupScript();
        return ResponseEntity.ok(new CyrevApiResponse<>(true, "Exchange Online setup script generated", script));
    }

    @GetMapping("/exchange-setup-verify")
    @RelationshipManager
    public ResponseEntity<CyrevApiResponse<Boolean>> exchangeSetupVerify() {
        boolean ready = exchangeBootstrapService.verifySetup();
        String message = ready
                ? "Exchange Online access is set up correctly"
                : "Exchange Online access isn't set up yet — run the setup script and try again in a few minutes";
        return ResponseEntity.ok(new CyrevApiResponse<>(ready, message, ready));
    }

}
