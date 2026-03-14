package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.entra.service.clients.ResilientGraphClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupRemoveStrategy implements GovernanceStrategy {

    private final ResilientGraphClient graph;

    @Override
    public GovernanceOperationType getOperationType() {
        return GovernanceOperationType.GROUP_REMOVE;
    }

    @Override
    public void execute(GovernanceRequestEntity request) {

        graph.delete(
                request.getTenantId(),
                "/groups/" + request.getTargetId() +
                        "/members/" + request.getPrincipalId() + "/$ref"
        );
    }
}