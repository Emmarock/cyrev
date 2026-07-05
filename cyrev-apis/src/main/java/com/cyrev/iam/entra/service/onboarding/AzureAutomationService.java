package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.common.dtos.AutomationJobStatus;
import com.cyrev.common.dtos.DistributionGroupRequest;
import com.cyrev.common.dtos.SharedMailboxRequest;
import com.cyrev.iam.config.AzureAutomationProperties;
import com.cyrev.iam.config.EntraProperties;
import com.cyrev.iam.entra.service.EntraOrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureAutomationService {

    private static final String ARM_BASE = "https://management.azure.com";
    private static final String API_VERSION = "2019-06-01";

    private static final String RUNBOOK_ADD_TO_MAILBOX      = "EXO-addUserToSharedMailbox";
    private static final String RUNBOOK_REMOVE_FROM_MAILBOX = "EXO-removeUserFromSharedMailbox";
    private static final String RUNBOOK_ADD_TO_DG           = "EXO-AddUserToDistributiongroup";
    private static final String RUNBOOK_REMOVE_FROM_DG      = "EXO-RemoveUserFromDistributiongroup";

    private final AzureAutomationProperties automationProps;
    private final EntraProperties entraProps;
    private final TenantAccessTokenService tokenService;
    private final ExchangeBootstrapService exchangeBootstrapService;
    private final EntraOrganizationService organizationService;
    private final WebClient.Builder webClientBuilder;

    public String runExchangeSetup(String tenantId) {
        String runbook = automationProps.getExchangeSetupRunbook();
        if (runbook == null || runbook.isBlank()) {
            throw new IllegalStateException("AUTOMATION_EXCHANGE_SETUP_RUNBOOK is not configured");
        }
        String spObjectId = exchangeBootstrapService.lookupServicePrincipalObjectId(tenantId);
        return startRunbook(runbook, Map.of(
                "TenantId",   resolveDomain(tenantId),
                "AppId",      entraProps.getAppId(),
                "SpObjectId", spObjectId
        ));
    }

    public String addUserToSharedMailbox(String tenantId, SharedMailboxRequest req) {
        Map<String, Object> params = new HashMap<>();
        params.put("TenantId",     resolveDomain(tenantId));
        params.put("SharedMailbox", req.getSharedMailbox());
        params.put("User",          req.getUser());
        if (req.getFullAccess()    != null) params.put("FullAccess",    req.getFullAccess());
        if (req.getSendAs()        != null) params.put("SendAs",        req.getSendAs());
        if (req.getSendOnBehalf()  != null) params.put("SendOnBehalf",  req.getSendOnBehalf());
        return startRunbook(RUNBOOK_ADD_TO_MAILBOX, params);
    }

    public String removeUserFromSharedMailbox(String tenantId, SharedMailboxRequest req) {
        Map<String, Object> params = new HashMap<>();
        params.put("TenantId",     resolveDomain(tenantId));
        params.put("SharedMailbox", req.getSharedMailbox());
        params.put("User",          req.getUser());
        if (req.getFullAccess()    != null) params.put("FullAccess",    req.getFullAccess());
        if (req.getSendAs()        != null) params.put("SendAs",        req.getSendAs());
        if (req.getSendOnBehalf()  != null) params.put("SendOnBehalf",  req.getSendOnBehalf());
        return startRunbook(RUNBOOK_REMOVE_FROM_MAILBOX, params);
    }

    public String addUserToDistributionGroup(String tenantId, DistributionGroupRequest req) {
        return startRunbook(RUNBOOK_ADD_TO_DG, Map.of(
                "TenantId",          resolveDomain(tenantId),
                "DistributionGroup", req.getDistributionGroup(),
                "User",              req.getUser()
        ));
    }

    public String removeUserFromDistributionGroup(String tenantId, DistributionGroupRequest req) {
        return startRunbook(RUNBOOK_REMOVE_FROM_DG, Map.of(
                "TenantId",          resolveDomain(tenantId),
                "DistributionGroup", req.getDistributionGroup(),
                "User",              req.getUser()
        ));
    }

    @SuppressWarnings("unchecked")
    public AutomationJobStatus getJobStatus(String jobId) {
        String jobPath = accountPath() + "/jobs/" + jobId + "?api-version=" + API_VERSION;

        Map<String, Object> response = buildArmClient()
                .get()
                .uri(jobPath)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Object> props = (Map<String, Object>) response.get("properties");
        return AutomationJobStatus.builder()
                .jobId(jobId)
                .status((String) props.get("status"))
                .statusDetails((String) props.get("statusDetails"))
                .startTime((String) props.get("startTime"))
                .endTime((String) props.get("endTime"))
                .exception((String) props.get("exception"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private String startRunbook(String runbookName, Map<String, Object> parameters) {
        String uri = accountPath() + "/runbooks/" + runbookName + "/start?api-version=" + API_VERSION;

        Map<String, Object> response = buildArmClient()
                .post()
                .uri(uri)
                .bodyValue(Map.of("properties", Map.of("parameters", parameters)))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String jobId = (String) response.get("name");
        log.info("Started runbook={} jobId={} params={}", runbookName, jobId, parameters.keySet());
        return jobId;
    }

    // The runbook tenantId parameter expects the vanity domain (e.g. cyrev.onmicrosoft.com),
    // not the GUID, so we resolve it via the Graph /organization endpoint.
    private String resolveDomain(String tenantId) {
        return organizationService.getPrimaryDomain(tenantId);
    }

    private String accountPath() {
        return "/subscriptions/" + automationProps.getSubscriptionId()
                + "/resourceGroups/" + automationProps.getResourceGroup()
                + "/providers/Microsoft.Automation/automationAccounts/" + automationProps.getAccountName();
    }

    private WebClient buildArmClient() {
        String token = tokenService.getTenantArmAccessToken(entraProps.getTenantId());
        return webClientBuilder
                .baseUrl(ARM_BASE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
