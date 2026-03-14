package com.cyrev.common.dtos;

import lombok.Data;

@Data
public class InviteUserRequest {

    private String firstName;

    private String lastName;

    private String businessEmail;

    private Role role;
}