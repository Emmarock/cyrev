package com.cyrev.common.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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