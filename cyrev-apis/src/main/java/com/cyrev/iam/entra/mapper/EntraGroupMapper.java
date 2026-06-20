package com.cyrev.iam.entra.mapper;

import com.cyrev.common.dtos.EntraGroup;

import java.util.Map;

public class EntraGroupMapper {
    public static EntraGroup fromGraph(Map<String, Object> data){
        return EntraGroup.builder()
                .id((String) data.get("id"))
                .displayName((String) data.get("displayName"))
                .mailNickname((String) data.get("mailNickname"))
                .securityEnabled((Boolean) data.get("securityEnabled"))
                .build();
    }
}
