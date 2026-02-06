package com.cyrev.iam.scim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScimUserResponse {

    private final List<String> schemas =
        List.of("urn:ietf:params:scim:schemas:core:2.0:User");

    private String id;
    private String userName;
    private Boolean active;
    private Meta meta;
}
