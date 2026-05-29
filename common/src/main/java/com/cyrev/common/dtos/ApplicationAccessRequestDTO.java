package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationAccessRequestDTO {

    @NotBlank(message = "servicePrincipalId is required")
    private String servicePrincipalId;

    @NotBlank(message = "appRoleId is required")
    private String appRoleId;

    private String justification;
}