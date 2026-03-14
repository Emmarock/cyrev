package com.cyrev.iam;

import com.cyrev.common.repository.GovernanceRequestRepository;
import com.cyrev.common.repository.SaasTenantRepository;
import com.cyrev.iam.governance.TestTenantFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected GovernanceRequestRepository governanceRequestRepository;

    @Autowired
    private SaasTenantRepository tenantRepository;

    protected String testTenantId;

    @BeforeEach
    void setupTenant() {
        governanceRequestRepository.deleteAll();
        testTenantId = UUID.randomUUID().toString();
        tenantRepository.save(TestTenantFactory.create(testTenantId));
    }
}