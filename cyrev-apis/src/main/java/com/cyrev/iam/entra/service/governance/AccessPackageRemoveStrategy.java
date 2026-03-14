package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AccessPackageRemoveStrategy implements GovernanceStrategy {

    private final ResilientGraphClient graph;

    @Override
    public GovernanceOperationType getOperationType() {
        return GovernanceOperationType.ACCESS_PACKAGE_REMOVE;
    }

    @Override
    public void execute(GovernanceRequestEntity request) {

        Map<String, Object> body = Map.of(
                "requestType", "adminRemove",
                "accessPackageAssignment", Map.of(
                        "targetId", request.getPrincipalId(),
                        "accessPackageId", request.getTargetId()
                )
        );

        graph.post(
                request.getTenantId(),
                "/identityGovernance/entitlementManagement/accessPackageAssignmentRequests",
                body
        );
    }
}