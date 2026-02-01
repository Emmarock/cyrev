package com.cyrev.iam.scim;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ScimFullIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${scim.token}")
    private String scimToken;

    private String userId;
    private String groupId;

    @Test
    void testFullUserLifecycle() throws Exception {
        // Create User
        String userJson = """
            {
              "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
              "externalId": "aad-user-123",
              "userName": "jane.doe@company.com",
              "name": {"givenName": "Jane", "familyName": "Doe"},
              "emails": [{"value": "jane.doe@company.com", "primary": true}],
              "active": true
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/scim/v2/Users")
                .header("Authorization", "Bearer " + scimToken)
                .contentType("application/scim+json")
                .accept("application/scim+json")
                .content(userJson))
            .andExpect(status().isCreated())
            .andReturn();

        userId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        // Patch User
        String patchJson = """
            {
              "schemas": ["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
              "Operations": [{"op": "Replace", "path": "active", "value": false}]
            }
            """;

        mockMvc.perform(patch("/scim/v2/Users/" + userId)
                .header("Authorization", "Bearer " + scimToken)
                .contentType("application/scim+json")
                .accept("application/scim+json")
                .content(patchJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));

        // Delete User
        mockMvc.perform(delete("/scim/v2/Users/" + userId)
                .header("Authorization", "Bearer " + scimToken))
            .andExpect(status().isNoContent());
    }

    @Test
    void testFullGroupLifecycle() throws Exception {
        // Create Group
        String groupJson = """
            {
              "schemas": ["urn:ietf:params:scim:schemas:core:2.0:Group"],
              "displayName": "Engineering",
              "members": []
            }
            """;

        MvcResult createGroup = mockMvc.perform(post("/scim/v2/Groups")
                .header("Authorization", "Bearer " + scimToken)
                .contentType("application/scim+json")
                .accept("application/scim+json")
                .content(groupJson))
            .andExpect(status().isCreated())
            .andReturn();

        groupId = JsonPath.read(createGroup.getResponse().getContentAsString(), "$.id");

        // Patch Group: add member
        String patchGroupJson = """
            {
              "schemas": ["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
              "Operations": [{"op": "Add", "path": "members", "value": [{"value": "%s"}]}]
            }
            """.formatted(UUID.randomUUID().toString()); // Replace with existing userId in real test

        mockMvc.perform(patch("/scim/v2/Groups/" + groupId)
                .header("Authorization", "Bearer " + scimToken)
                .contentType("application/scim+json")
                .accept("application/scim+json")
                .content(patchGroupJson))
            .andExpect(status().isOk());

        // Delete Group
        mockMvc.perform(delete("/scim/v2/Groups/" + groupId)
                .header("Authorization", "Bearer " + scimToken))
            .andExpect(status().isNoContent());
    }
}
