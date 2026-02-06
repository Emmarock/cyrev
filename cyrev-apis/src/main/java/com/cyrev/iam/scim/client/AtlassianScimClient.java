package com.cyrev.iam.scim.client;

import com.cyrev.common.dtos.ScimUser;
import com.cyrev.iam.scim.dto.ScimListResponse;
import com.cyrev.iam.scim.dto.ScimPatchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AtlassianScimClient {

    private final WebClient jiraWebClient;

    @Value("${scim.atlassian.base-url}")
    private String baseUrl;

    @Value("${scim.atlassian.directory-id}")
    private String directoryId;

    private String url(String path) {
        return baseUrl + "/" + directoryId + path;
    }

    public Optional<ScimUser> findByEmail(String email) {
        return jiraWebClient.get()
                .uri(url("/Users?filter=userName eq \"" + email + "\""))
                .retrieve()
                .bodyToMono(ScimListResponse.class)
                .map(r -> r.getResources().stream().findFirst())
                .block();
    }

    public ScimUser createUser(ScimUser user) {
        return jiraWebClient.post()
                .uri(url("/Users"))
                .bodyValue(user)
                .retrieve()
                .bodyToMono(ScimUser.class)
                .block();
    }

    public void deactivate(String userId) {
        jiraWebClient.patch()
                .uri(url("/Users/" + userId))
                .bodyValue(Map.of(
                        "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
                        "Operations", List.of(Map.of(
                                "op", "Replace",
                                "path", "active",
                                "value", false
                        ))
                ))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
    /**
     * Add user to groups (idempotent)
     */
    public void addUserToGroups(String scimUserId, List<String> groups) {

        for (String group : groups) {
            addUserToGroup(scimUserId, group);
        }
    }

    private void addUserToGroup(String scimUserId, String group) {

        ScimPatchRequest request = ScimPatchRequest.builder()
                .schemas(List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"))
                .operations(List.of(
                        ScimPatchRequest.PatchOperation.builder()
                                .op("Add")
                                .path("groups")
                                .value(List.of(Map.of("value", group)))
                                .build()
                ))
                .build();

        try {
            jiraWebClient.patch()
                    .uri("/Users/{id}", scimUserId)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            resp -> resp.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("SCIM 4xx: " + body))
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            resp -> resp.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("SCIM 5xx: " + body))
                    )
                    .toBodilessEntity()
                    .block();

            log.info("Added user={} to group={}", scimUserId, group);

        } catch (Exception e) {
            // Atlassian returns 409 or ignores duplicates → safe to continue
            if (isAlreadyMemberError(e)) {
                log.info("User={} already in group={}", scimUserId, group);
                return;
            }
            throw e;
        }
    }

    private boolean isAlreadyMemberError(Exception e) {
        return e.getMessage() != null &&
                e.getMessage().contains("already") ||
                e.getMessage().contains("exists");
    }
}
