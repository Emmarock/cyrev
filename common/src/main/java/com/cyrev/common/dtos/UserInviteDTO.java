package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserInviteDTO {
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private InviteStatus status;
    private Instant expiresAt;
}
