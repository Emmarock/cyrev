package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateBusinessUserDTO {

    @NotNull(message = "Business id is required")
    private UUID businessId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private UUID managerId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String unit;
    private String department;
    private String division;
}