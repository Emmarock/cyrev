package com.cyrev.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDTO {

    // profile fields — any authenticated user
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String secret;
    private Boolean mfaEnabled;

    // privileged fields — tenant admin only
    private Role role;
    private UserStatus status;
}
