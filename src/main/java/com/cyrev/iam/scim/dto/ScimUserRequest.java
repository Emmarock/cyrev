package com.cyrev.iam.scim.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScimUserRequest {
    private List<String> schemas;
    private String userName;
    private Name name;
    private List<Email> emails;
    private Boolean active;
    private String externalId;

    @Data
    public static class Name {
        private String givenName;
        private String familyName;
    }

    @Data
    public static class Email {
        private String value;
        private Boolean primary;
    }
}
