package com.cyrev.iam.orchestration;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.ProvisioningState;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProvisioningStateService {

    public void markProvisioning(UUID userId, App app) {
        updateState(userId, app, ProvisioningState.PROVISIONING);
    }

    public void markSuccess(UUID userId, App app) {
        updateState(userId, app, ProvisioningState.PROVISIONED);
    }

    public void markFailure(UUID userId, App app, String error) {
        updateState(userId, app, ProvisioningState.FAILED);
    }

    private void updateState(UUID userId, App app, ProvisioningState state) {
        // transactional update
    }
}
