package com.cyrev.common.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Organization code is required")
    private String organizationCode; // link to Organization

    private UUID managerId; // optional


    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotBlank(message = "Division is required")
    private String division;

    private Set<@NotBlank(message = "App type cannot be blank") String> assignedApps;

    @NotNull(message = "Identity status is required")
    private IdentityStatus identityStatus; // JOINER, ACTIVE, PRE_LEAVER, LEAVER

    @NotBlank(message = "Role is required")
    private String role;
}
