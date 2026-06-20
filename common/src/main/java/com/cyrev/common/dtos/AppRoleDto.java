package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppRoleDto {
    private String id;
    private String displayName;
    private String description;
}
