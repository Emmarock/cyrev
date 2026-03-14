package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppRoleAssignStrategy implements GovernanceStrategy {

    private final ResilientGraphClient graph;

    @Override
    public GovernanceOperationType getOperationType() {
        return GovernanceOperationType.APP_ROLE_ASSIGN;
    }

    @Override
    public void execute(GovernanceRequestEntity request) {

        Map<String, Object> body = Map.of(
                "principalId", request.getPrincipalId(),
                "resourceId", request.getTargetId(),  // servicePrincipalId
                "appRoleId", request.getAdditionalId()
        );

        graph.post(
                request.getTenantId(),
                "/users/" + request.getPrincipalId() + "/appRoleAssignments",
                body
        );
    }
}