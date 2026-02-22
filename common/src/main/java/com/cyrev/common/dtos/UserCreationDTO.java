package com.cyrev.common.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationDTO {

    @NotBlank(message = "Business Email is required")
    @Email(message = "Invalid email format")
    private String businessEmail;

    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Organization is required")
    private OrganizationCreationDTO organization;

    @NotNull(message = "company address is required")
    private AddressDto companyAddress;
}
