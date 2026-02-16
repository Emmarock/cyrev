package com.cyrev.common.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class OrganizationCreationDTO {

    @NotBlank(message = "Organization code is required")
    @Size(max = 20, message = "Organization code must be at most 20 characters")
    private String code;

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must be at most 100 characters")
    private String name;
}
