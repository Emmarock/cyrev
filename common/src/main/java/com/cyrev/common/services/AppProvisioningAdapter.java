package com.cyrev.common.services;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.User;

import java.util.Optional;

public interface AppProvisioningAdapter {

    App app();

    void createUser(User user, Role role);

    void assignUser(User user, Role role);

    void revokeUser(User user);

    Optional<String> fetchExternalUserIdByEmail(String email);
}
