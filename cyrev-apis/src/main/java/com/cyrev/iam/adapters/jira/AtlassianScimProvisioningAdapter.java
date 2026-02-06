package com.cyrev.iam.adapters.jira;

import com.cyrev.common.services.AppProvisioningAdapter;
import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.dtos.ScimUser;
import com.cyrev.common.entities.User;
import com.cyrev.iam.scim.client.AtlassianScimClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AtlassianScimProvisioningAdapter implements AppProvisioningAdapter {

    private final AtlassianScimClient scimClient;

    @Override
    public App app() {
        return App.UNKNOWN;
    }

    @Override
    public void createUser(User user, Role role) {
        scimClient.findByEmail(user.getEmail())
                .ifPresentOrElse(
                        existing -> {
                            // already exists → ensure active
                        },
                        () -> {
                            ScimUser user1 = scimClient.createUser(
                                    ScimUser.from(user)
                            );
                        }
                );
    }

    @Override
    public void assignUser(User user, Role role) {
        createUser(user,role);
    }


    @Override
    public void revokeUser(User user) {
        scimClient.findByEmail(user.getEmail())
                .ifPresent(u -> scimClient.deactivate(u.getId()));
    }

    @Override
    public Optional<String> fetchExternalUserIdByEmail(String email) {
        return scimClient.findByEmail(email).map(ScimUser::getId);
    }

}
