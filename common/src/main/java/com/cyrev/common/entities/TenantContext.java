package com.cyrev.common.entities;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TenantContext {
    private final String entraTenantId;
    private final UUID internalTenantId;
    private final String plan;
    private final String userObjectId;
}