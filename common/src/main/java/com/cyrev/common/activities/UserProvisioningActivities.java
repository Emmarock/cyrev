package com.cyrev.common.activities;


import com.cyrev.common.dtos.IdentityStatus;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
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

    void activateUser(UUID userId);
}
