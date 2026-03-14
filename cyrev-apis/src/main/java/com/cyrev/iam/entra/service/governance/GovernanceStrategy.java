package com.cyrev.iam.entra.service.governance;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.entities.GovernanceRequestEntity;

public interface GovernanceStrategy {

    GovernanceOperationType getOperationType();

    void execute(GovernanceRequestEntity request);
}