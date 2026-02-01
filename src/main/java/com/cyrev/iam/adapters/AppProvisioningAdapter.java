package com.cyrev.iam.adapters;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Role;
import com.cyrev.iam.entities.User;

import java.util.Optional;

public interface AppProvisioningAdapter {

    App app();

    void createUser(User user, Role role);

    void assignUser(User user, Role role);

    void revokeUser(User user);

    Optional<String> fetchExternalUserIdByEmail(String email);
}
