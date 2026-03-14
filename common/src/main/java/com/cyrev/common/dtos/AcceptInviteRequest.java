package com.cyrev.common.dtos;

import lombok.Data;

@Data
public class AcceptInviteRequest {

    private String token;

    private String password;
}