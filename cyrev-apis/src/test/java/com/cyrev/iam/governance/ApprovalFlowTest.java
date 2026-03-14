package com.cyrev.iam.governance;

import com.cyrev.common.dtos.ApprovalStatus;
import com.cyrev.common.dtos.GovernanceOperationType;
import com.cyrev.common.dtos.GovernanceStatus;
import com.cyrev.common.entities.GovernanceRequestEntity;
import com.cyrev.iam.BaseIntegrationTest;
import com.cyrev.iam.security.TestJwtFactory;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApprovalFlowTest extends BaseIntegrationTest {

    @Test
    void shouldApproveRequest() throws Exception {

        // Insert request
        GovernanceRequestEntity request = new GovernanceRequestEntity();
        request.setTenantId(testTenantId);
        request.setOperationType(GovernanceOperationType.GROUP_ADD);
        request.setApprovalRequired(true);
        request.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
        request.setStatus(GovernanceStatus.PENDING);

        request = governanceRequestRepository.save(request);

        mockMvc.perform(post("/api/approvals/" + request.getId() + "/approve")
                        .with(TestJwtFactory.tenantAdmin(testTenantId)))
                .andExpect(status().isOk());

        GovernanceRequestEntity updated = governanceRequestRepository.findById(request.getId()).orElseThrow();

        assertEquals(ApprovalStatus.APPROVED, updated.getApprovalStatus());
    }
    @Test
    void shouldRejectIfNotAdmin() throws Exception {

        mockMvc.perform(post("/api/approvals/123e4567-e89b-12d3-a456-426614174000/approve")
                        .with(TestJwtFactory.tenantAuditor(testTenantId)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void shouldBlockCrossTenantApproval() throws Exception {

        GovernanceRequestEntity request = new GovernanceRequestEntity();
        request.setTenantId("tenant-ABC");
        request.setApprovalRequired(true);
        request.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
        request.setStatus(GovernanceStatus.PENDING);

        request = governanceRequestRepository.save(request);

        mockMvc.perform(post("/api/approvals/" + request.getId() + "/approve")
                        .with(TestJwtFactory.tenantAdmin(testTenantId)))
                .andExpect(status().isForbidden());
    }

}