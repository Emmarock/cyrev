package com.cyrev.iam.domain;

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
public class UserUpdateRequestDTO {

    private UUID managerId;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotBlank(message = "Division is required")
    private String division;
}
