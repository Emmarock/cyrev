package com.cyrev.iam.scim.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScimGroupRequest {

    private List<String> schemas;

    private String externalId;     // Entra objectId
    private String displayName;

    private List<Member> members;  // Optional (usually empty on create)

    @Data
    public static class Member {
        private String value; // user SCIM id
    }
}
