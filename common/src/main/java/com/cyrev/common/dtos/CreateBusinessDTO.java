package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBusinessDTO {

    @NotBlank(message = "Org code is required")
    @Pattern(regexp = "^[A-Z0-9-]{2,16}$",
            message = "Org code must be 2-16 uppercase letters, digits, or dashes")
    private String orgCode;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Contract start date is required")
    private LocalDate contractStartDate;
    @NotNull(message = "Contract end date is required")
    private LocalDate contractEndDate;

    @NotNull(message = "Relationship owner is required")
    private UUID relationshipOwnerId;

    private String employeeIdFormat;
}