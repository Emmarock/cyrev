package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccessPackageAccessRequestDTO {

    @NotBlank(message = "accessPackageId is required")
    private String accessPackageId;

    private String justification;
}