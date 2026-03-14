package com.cyrev.iam.entra.service.utils;

import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.dtos.GovernanceStatus;
import com.cyrev.common.entities.TenantAuditLog;
import com.cyrev.common.repository.TenantAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final TenantAuditLogRepository repo;

    public void log(String tenantId,
                    GovernanceOperationType action,
                    String referenceId,
                    GovernanceStatus status) {

        TenantAuditLog log = new TenantAuditLog();
        log.setTenantId(tenantId);
        log.setAction(action);
        log.setActor("SYSTEM");
        log.setReferenceId(referenceId);
        log.setStatus(status);
        log.setTimestamp(LocalDateTime.now());

        repo.save(log);
    }
}