package com.cyrev.iam.scim.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScimGroupResponse {

    private List<String> schemas =
        List.of("urn:ietf:params:scim:schemas:core:2.0:Group");

    private String id;
    private String externalId;
    private String displayName;
    private List<Member> members;

    @Data
    public static class Member {
        private String value; // user SCIM ID
    }
}
