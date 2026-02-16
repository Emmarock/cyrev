package com.cyrev.iam.entra.service;

import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntraRoleService {

    private final GraphServiceClient<?> graphClient;

    public AppRoleAssignment assignRoleToUser(String userId, String principalId, String appRoleId, String resourceId) {
        AppRoleAssignment assignment = new AppRoleAssignment();
        assignment.principalId = java.util.UUID.fromString(principalId);
        assignment.resourceId = java.util.UUID.fromString(resourceId);
        assignment.appRoleId = java.util.UUID.fromString(appRoleId);

        return graphClient.users(userId).appRoleAssignments()
                .buildRequest()
                .post(assignment);
    }
}
