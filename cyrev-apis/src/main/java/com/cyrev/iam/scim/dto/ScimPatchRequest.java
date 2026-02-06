package com.cyrev.iam.scim.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScimPatchRequest {
    private List<String> schemas;
    private List<PatchOperation> operations;

    @Data
    @Builder
    public static class PatchOperation {
        private String op;
        private String path;
        private Object value;
    }
}
