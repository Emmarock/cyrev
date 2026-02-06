package com.cyrev.iam.adapters.slack;

import com.cyrev.common.services.AppProvisioningAdapter;
import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Component
public class SlackAdapter implements AppProvisioningAdapter {

    private final WebClient slackWebClient;

    public SlackAdapter( @Qualifier("slackWebClient") WebClient slackWebClient) {
        this.slackWebClient = slackWebClient;
    }

    @Override
    public App app() {
        return App.SLACK;
    }

    @Override
    public void createUser(User user, Role role) {
        if (fetchExternalUserIdByEmail(user.getEmail()).isPresent()) {
            return; // idempotent
        }

        slackWebClient.post()
            .uri("/users.admin.invite")
            .bodyValue(Map.of(
                "email", user.getEmail(),
                "real_name", user.getFirstName() + " " + user.getLastName()
            ))
            .retrieve()
            .bodyToMono(Void.class).block();
    }

    @Override
    public void assignUser(User user, Role role) {
        // Slack assignment == invite (no-op if exists)
        createUser(user, role);
    }

    @Override
    public void revokeUser(User user) {
        fetchExternalUserIdByEmail(user.getEmail())
            .ifPresent(slackId ->
                slackWebClient.post()
                    .uri("/users.admin.setInactive")
                    .bodyValue(Map.of("user_id", slackId))
                    .retrieve()
                    .bodyToMono(Void.class).block()
            );
    }

    @Override
    public Optional<String> fetchExternalUserIdByEmail(String email) {
        try {
            Map<?, ?> response = slackWebClient.get()
                .uri(uriBuilder ->
                    uriBuilder.path("/users.lookupByEmail")
                        .queryParam("email", email)
                        .build()
                )
                .retrieve()
                .bodyToMono(Map.class)
                    .block();

            return Optional.ofNullable(
                ((Map<?, ?>) response.get("user")).get("id").toString()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
