package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AppRoleRemoveStrategy implements GovernanceStrategy {

    private final ResilientGraphClient graph;

    @Override
    public GovernanceOperationType getOperationType() {
        return GovernanceOperationType.APP_ROLE_REMOVE;
    }

    @Override
    public void execute(GovernanceRequestEntity request) {

        // Retrieve assignmentId
        Map response = graph.get(
                request.getTenantId(),
                "/users/" + request.getPrincipalId() + "/appRoleAssignments"
        );

        List<Map<String, Object>> assignments =
                (List<Map<String, Object>>) response.get("value");

        String assignmentId = assignments.stream()
                .filter(a -> request.getTargetId().equals(a.get("resourceId")))
                .map(a -> (String) a.get("id"))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("App role assignment not found"));

        graph.delete(
                request.getTenantId(),
                "/users/" + request.getPrincipalId() + "/appRoleAssignments/" + assignmentId
        );
    }
}