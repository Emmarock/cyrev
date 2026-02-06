package com.cyrev.iam.scim.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScimListResponse<T> {
    private final List<String> schemas =
        List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse");

    private int totalResults;
    private List<T> Resources;
}
