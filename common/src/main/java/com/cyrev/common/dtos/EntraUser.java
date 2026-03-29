package com.cyrev.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntraUser {

    private String id;

    private String displayName;
    private String givenName;
    private String familyName;
    private String userPrincipalName;
    private String password;

    private String mail;

    private Boolean accountEnabled;
}