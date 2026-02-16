package com.cyrev.iam.entra.service;

import com.microsoft.graph.models.Group;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntraGroupService {

    private final GraphServiceClient<?> graphClient;

    public Group createGroup(String displayName, String mailNickname) {
        Group group = new Group();
        group.displayName = displayName;
        group.mailEnabled = false;
        group.securityEnabled = true;
        group.mailNickname = mailNickname;

        return graphClient.groups()
                .buildRequest()
                .post(group);
    }

    public void deleteGroup(String groupId) {
        graphClient.groups(groupId)
                .buildRequest()
                .delete();
    }
}
