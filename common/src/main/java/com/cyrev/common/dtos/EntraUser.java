package com.cyrev.common.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntraUser {

    private String id;

    private String displayName;
    private String givenName;
    private String familyName;
    private String userPrincipalName;
    private String password;

    private String mail;

    private boolean accountEnabled;
}