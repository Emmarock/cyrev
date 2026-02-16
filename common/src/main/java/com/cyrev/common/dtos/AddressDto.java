package com.cyrev.common.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDto {
    @NotBlank(message = "building number is required")
    private String buildingNumber;
    @NotBlank(message = "Street name is required")
    private String street;
    @NotBlank(message = "City name is required")
    private String city;
    @NotBlank(message = "state is required")
    private String state;
    @NotBlank
    private String postalCode;
    @NotBlank
    private String countryCode;
    @NotBlank
    private String countryName;
}
