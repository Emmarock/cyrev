package com.cyrev.iam.governance;


import com.cyrev.iam.BaseIntegrationTest;
import com.cyrev.iam.security.TestJwtFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GovernanceSubmitTest extends BaseIntegrationTest {

    @Test
    void shouldSubmitGovernanceRequest() throws Exception {

        String body = """
            {
              "operationType": "GROUP_ADD",
              "targetId": "group-1",
              "principalId": "user-1",
              "approvalRequired": true
            }
        """;

        mockMvc.perform(post("/api/governance/submit")
                .with(TestJwtFactory.tenantAdmin(testTenantId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
        assertEquals(1, governanceRequestRepository.findAll().size());
    }
    @Test
    void shouldSubmitBulkRequest() throws Exception {

        String body = """
        {
          "operationType": "APP_ROLE_ASSIGN",
          "targetId": "sp-1",
          "principalIds": ["user1", "user2"],
          "approvalRequired": false
        }
    """;

        mockMvc.perform(
                        post("/api/governance/submit/bulk")
                                .with(TestJwtFactory.tenantAdmin(testTenantId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isAccepted());
    }
}