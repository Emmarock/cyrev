package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GroupAddStrategy implements GovernanceStrategy {

    private final ResilientGraphClient graph;

    @Override
    public GovernanceOperationType getOperationType() {
        return GovernanceOperationType.GROUP_ADD;
    }

    @Override
    public void execute(GovernanceRequestEntity request) {

        Map<String, String> body = Map.of(
                "@odata.id",
                "https://graph.microsoft.com/v1.0/directoryObjects/"
                        + request.getPrincipalId()
        );

        graph.post(
                request.getTenantId(),
                "/groups/" + request.getTargetId() + "/members/$ref",
                body
        );
    }
}