package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServicePrincipalDto {
    private String id;
    private String displayName;
    private List<AppRoleDto> appRoles;
}
