package com.cyrev.iam.governance;

import com.cyrev.common.dtos.TenantStatus;
import com.cyrev.common.entities.SaasTenant;

import java.time.Instant;
import java.util.UUID;

public class TestTenantFactory {

    public static SaasTenant create(String entraTid) {
        SaasTenant tenant = new SaasTenant();
        tenant.setId(UUID.randomUUID());
        tenant.setTenantId(UUID.randomUUID());
        tenant.setEntraTenantId(entraTid);
        tenant.setPlan("ENTERPRISE");
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setConsentGranted(true);
        tenant.setCreatedAt(Instant.now());
        tenant.setUpdatedAt(Instant.now());
        return tenant;
    }
}