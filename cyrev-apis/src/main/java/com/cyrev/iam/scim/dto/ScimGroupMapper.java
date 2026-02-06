package com.cyrev.iam.scim.dto;

import com.cyrev.common.entities.Group;
import com.cyrev.common.entities.User;
import org.springframework.stereotype.Component;

@Component
public class ScimGroupMapper {

    public ScimGroupResponse toScim(Group group) {

        ScimGroupResponse resp = new ScimGroupResponse();
        resp.setId(group.getId().toString());
        resp.setExternalId(group.getExternalId());
        resp.setDisplayName(group.getDisplayName());

        resp.setMembers(
            group.getMembers().stream()
                .map(this::toMember)
                .toList()
        );

        return resp;
    }

    private ScimGroupResponse.Member toMember(User user) {
        ScimGroupResponse.Member m = new ScimGroupResponse.Member();
        m.setValue(user.getId().toString());
        return m;
    }
}
