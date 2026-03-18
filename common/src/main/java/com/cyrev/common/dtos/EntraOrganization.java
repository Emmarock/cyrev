package com.cyrev.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntraOrganization {

    private String id;
    private String displayName;
    private String tenantType;
    private List<String> verifiedDomains;
}