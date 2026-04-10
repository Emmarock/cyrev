package com.cyrev.common.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteSignupDTO {

    @NotBlank(message = "Business Email is required")
    @Email(message = "Invalid email format")
    private String businessEmail;

    @NotNull(message = "company address is required")
    private AddressDto companyAddress;

    private AuthProvider authProvider;
}
