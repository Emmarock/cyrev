package com.cyrev.iam.temporal.activity;

import com.cyrev.iam.domain.IdentityStatus;
import com.cyrev.iam.domain.UserCreationDTO;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@ActivityInterface
public interface UserProvisioningActivities {

    @ActivityMethod
    void createUser(UUID userId);

    @ActivityMethod
    void assignEmployeeId(UUID userId);

    @ActivityMethod
    void assignManager(UUID userId, UUID managerId);

    @ActivityMethod
    void setIdentityStatus(UUID userId, IdentityStatus status);

    @ActivityMethod
    void notifyUserCreation(UUID userId);

    @Transactional
    void activateUser(UUID userId);
}
