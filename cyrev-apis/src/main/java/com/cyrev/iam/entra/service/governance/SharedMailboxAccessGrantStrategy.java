package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.entra.service.clients.ResilientExchangeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Grants a user access to a shared mailbox via Exchange Online.
 *
 * <ul>
 *   <li>{@code principalId}: the user being granted access (UPN or object id)</li>
 *   <li>{@code targetId}: the shared mailbox (UPN or object id)</li>
 *   <li>{@code additionalId}: access right — {@code FullAccess} (default) or {@code SendAs}</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class SharedMailboxAccessGrantStrategy implements GovernanceStrategy {

    static final String FULL_ACCESS = "FullAccess";
    static final String SEND_AS = "SendAs";

    private final ResilientExchangeClient exchange;

    @Override
    public GovernanceOperationType getOperationType() {
        return GovernanceOperationType.SHARED_MAILBOX_ACCESS_GRANT;
    }

    @Override
    public void execute(GovernanceRequestEntity request) {
        String accessRight = request.getAdditionalId() == null
                || request.getAdditionalId().isBlank()
                ? FULL_ACCESS
                : request.getAdditionalId();

        if (SEND_AS.equalsIgnoreCase(accessRight)) {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("Identity", request.getTargetId());
            params.put("Trustee", request.getPrincipalId());
            params.put("AccessRights", List.of(SEND_AS));
            params.put("Confirm", false);

            exchange.invoke(request.getTenantId(), "Add-RecipientPermission", params);
            return;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("Identity", request.getTargetId());
        params.put("User", request.getPrincipalId());
        params.put("AccessRights", List.of(FULL_ACCESS));
        params.put("InheritanceType", "All");
        params.put("AutoMapping", true);

        exchange.invoke(request.getTenantId(), "Add-MailboxPermission", params);
    }
}