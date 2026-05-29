package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.entra.service.clients.ResilientExchangeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SharedMailboxAccessRevokeStrategy implements GovernanceStrategy {

    private final ResilientExchangeClient exchange;

    @Override
    public GovernanceOperationType getOperationType() {
        return GovernanceOperationType.SHARED_MAILBOX_ACCESS_REVOKE;
    }

    @Override
    public void execute(GovernanceRequestEntity request) {
        String accessRight = request.getAdditionalId() == null
                || request.getAdditionalId().isBlank()
                ? SharedMailboxAccessGrantStrategy.FULL_ACCESS
                : request.getAdditionalId();

        if (SharedMailboxAccessGrantStrategy.SEND_AS.equalsIgnoreCase(accessRight)) {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("Identity", request.getTargetId());
            params.put("Trustee", request.getPrincipalId());
            params.put("AccessRights", List.of(SharedMailboxAccessGrantStrategy.SEND_AS));
            params.put("Confirm", false);

            exchange.invoke(request.getTenantId(), "Remove-RecipientPermission", params);
            return;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("Identity", request.getTargetId());
        params.put("User", request.getPrincipalId());
        params.put("AccessRights", List.of(SharedMailboxAccessGrantStrategy.FULL_ACCESS));
        params.put("InheritanceType", "All");
        params.put("Confirm", false);

        exchange.invoke(request.getTenantId(), "Remove-MailboxPermission", params);
    }
}