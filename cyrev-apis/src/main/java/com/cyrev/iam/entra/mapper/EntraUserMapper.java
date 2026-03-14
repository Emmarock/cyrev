package com.cyrev.iam.entra.mapper;


import com.cyrev.common.dtos.EntraUser;

import java.util.Map;

public class EntraUserMapper {

    public static EntraUser fromGraph(Map<String, Object> data) {

        return EntraUser.builder()
                .id((String) data.get("id"))
                .displayName((String) data.get("displayName"))
                .userPrincipalName((String) data.get("userPrincipalName"))
                .mail((String) data.get("mail"))
                .accountEnabled((Boolean) data.get("accountEnabled"))
                .build();
    }
}