package com.cyrev.workflows.config;

import com.cyrev.activities.AppProvisioningActivitiesImpl;
import com.cyrev.common.activities.UserProvisioningActivities;
import com.cyrev.workflows.AppProvisioningWorkflowImpl;
import com.cyrev.workflows.UserProvisioningWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ConditionalOnProperty(
        name = "temporal.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TemporalWorkerConfig {

    @Value("${temporal.host:172.17.0.2}")
    private String temporalHost;

    @Value("${temporal.port:7233}")
    private int temporalPort;

    @Value("${temporal.namespace:temporal-system}")
    private String namespace;


    @Bean
    public WorkerFactory workerFactory(
            WorkflowClient client,
            AppProvisioningActivitiesImpl appProvisioningActivities,
            UserProvisioningActivities userProvisioningActivities // new activities bean
    ) {
        WorkerFactory factory = WorkerFactory.newInstance(client);

        // --- Worker for App Provisioning ---
        Worker appWorker = factory.newWorker("APP_PROVISION_TASK_QUEUE");
        appWorker.registerWorkflowImplementationTypes(AppProvisioningWorkflowImpl.class);
        appWorker.registerActivitiesImplementations(appProvisioningActivities);

        // --- Worker for User Provisioning ---
        Worker userWorker = factory.newWorker("USER_PROVISION_TASK_QUEUE");
        userWorker.registerWorkflowImplementationTypes(UserProvisioningWorkflowImpl.class);
        userWorker.registerActivitiesImplementations(userProvisioningActivities);

        factory.start();
        return factory;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
